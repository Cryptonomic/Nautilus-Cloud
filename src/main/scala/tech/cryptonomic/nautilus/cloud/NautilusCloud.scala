package tech.cryptonomic.nautilus.cloud

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{Found, NoContent, SeeOther}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import com.softwaremill.session.SessionConfig
import com.softwaremill.sttp.SttpBackendOptions.connectionTimeout
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor.Transactor
import pureconfig.generic.auto._
import pureconfig.loadConfig
import tech.cryptonomic.cloud.nautilus.adapters.akka.UserRoutes
import tech.cryptonomic.cloud.nautilus.adapters.akka.session.SessionOperations
import tech.cryptonomic.cloud.nautilus.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.adapters.akka.{ApiKeyRoutes, HttpConfig}
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.{GithubAuthenticationConfiguration, GithubConfig}
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.sttp.SttpGithubAuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.adapters.doobie.{DoobieApiKeyRepository, DoobieConfig, DoobieUserRepository}
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.Docs
import tech.cryptonomic.nautilus.cloud.domain.{ApiKeyService, AuthenticationService, UserService}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object NautilusCloud extends App with StrictLogging {

  lazy val githubConfig = loadConfig[GithubConfig](namespace = "security.auth.github").toOption.get
  lazy val authConfig = GithubAuthenticationConfiguration(githubConfig)
  lazy val doobieConfig = loadConfig[DoobieConfig](namespace = "doobie").toOption.get
  lazy val httpConfig = loadConfig[HttpConfig](namespace = "http").toOption.get
  lazy val xa = Transactor.fromDriverManager[IO]("org.postgresql.Driver", doobieConfig.url, doobieConfig.user, doobieConfig.password)

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val sttpBackend = AsyncHttpClientCatsBackend[IO](connectionTimeout(githubConfig.connectionTimeout))

  lazy val apiKeysRepository = new DoobieApiKeyRepository(xa)
  lazy val apiKeysService = new ApiKeyService[IO](apiKeysRepository)
  lazy val apiKeysRoutes = new ApiKeyRoutes(apiKeysService)
  lazy val userRepository = new DoobieUserRepository(xa)
  lazy val userService = new UserService[IO](userRepository, apiKeysRepository)
  lazy val userRoutes = new UserRoutes(userService)
  lazy val authRepository = new SttpGithubAuthenticationProviderRepository[IO](githubConfig)
  lazy val oauthService = new AuthenticationService[IO](authConfig, authRepository, userRepository)

  val sessionOperations: SessionOperations = new SessionOperations(SessionConfig.fromConfig())

  val route: Route = List(
    pathPrefix("docs") {
      pathEndOrSingleSlash {
        getFromResource("web/swagger/index.html")
      }
    },
    pathPrefix("swagger-ui") {
      getFromResourceDirectory("web/swagger/swagger-ui/")
    },
    Docs.route,
    path("") {
      redirect("/site", Found)
    },
    path("github-login") {
      redirect(oauthService.loginUrl, Found)
    },
    path("github-callback") {
      parameters('code) {
        code =>
          onComplete(oauthService.resolveAuthCode(code).unsafeToFuture()) {
            case Success(Right(session)) =>
              sessionOperations.setSession(session) { ctx =>
                ctx.redirect("/", SeeOther)
              }
            case Failure(exception) =>
              logger.error(exception.getMessage, exception)
              reject(AuthorizationFailedRejection)
            case Success(Left(exception)) =>
              logger.error(exception.getMessage, exception)
              reject(AuthorizationFailedRejection)
          }
      }
    },
    // @TODO should be removed when a proper login page is created
    pathPrefix("site") {
      getFromResource("web/index.html")
    },
    sessionOperations.requiredSession { session =>
      List(
        apiKeysRoutes.routes,
        sessionOperations.requiredRole(Administrator) {
          userRoutes.routes
        },
        path("logout") {
          post {
            sessionOperations.invalidateSession {
              complete(NoContent)
            }
          }
        },
        path("current_login") {
          get {
            complete(s"""{"email": "${session.email}", "role": "${session.role}"}""")
          }
        }
      ).reduce(_ ~ _)
    }
  ).reduce(_ ~ _)

  Http().bindAndHandle(route, httpConfig.host, httpConfig.port)
}
