package tech.cryptonomic.cloud.nautilus

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
import doobie.util.transactor.Transactor.Aux
import pureconfig.generic.auto._
import pureconfig.loadConfig
import tech.cryptonomic.cloud.nautilus.adapters.akka.{ApiKeyRoutes, UserRoutes}
import tech.cryptonomic.cloud.nautilus.adapters.doobie.{DoobieApiKeyRepository, DoobieConfig, DoobieUserRepository}
import tech.cryptonomic.cloud.nautilus.adapters.endpoints.Docs
import tech.cryptonomic.cloud.nautilus.adapters.sttp.{GithubConfig, SttpGithubRepository}
import tech.cryptonomic.cloud.nautilus.domain.user.Role.Administrator
import tech.cryptonomic.cloud.nautilus.domain.{ApiKeyService, SecurityService, UserService}
import tech.cryptonomic.cloud.nautilus.adapters.akka.session.SessionOperations

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Nautilus extends App with StrictLogging {

  lazy val githubConfig = loadConfig[GithubConfig](namespace = "security.github").toOption.get
  lazy val doobieConfig = loadConfig[DoobieConfig](namespace = "doobie").toOption.get
  lazy val xa: Aux[IO, Unit] = Transactor
    .fromDriverManager[IO]("org.postgresql.Driver", doobieConfig.url, doobieConfig.user, doobieConfig.password)

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val sttpBackend = AsyncHttpClientCatsBackend[IO](connectionTimeout(githubConfig.connectionTimeout))

  lazy val apiKeysRepo = new DoobieApiKeyRepository(xa)
  lazy val apiKeysService = new ApiKeyService[IO](apiKeysRepo)
  lazy val apiKeysRoutes = new ApiKeyRoutes(apiKeysService)
  lazy val userRepo = new DoobieUserRepository(xa)
  lazy val userService = new UserService[IO](userRepo, apiKeysRepo)
  lazy val userRoutes = new UserRoutes(userService)
  lazy val githubOauthRepository = new SttpGithubRepository[IO](githubConfig)
  lazy val oauthService = new SecurityService[IO](githubConfig, githubOauthRepository, userRepo)

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

  Http().bindAndHandle(route, "0.0.0.0", 1234)
}
