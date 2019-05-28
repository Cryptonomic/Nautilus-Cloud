package tech.cryptonomic.cloud.nautilus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{Found, NoContent, SeeOther, Unauthorized}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import com.softwaremill.session.SessionDirectives.setSession
import com.softwaremill.session.SessionOptions.{oneOff, usingCookies}
import com.softwaremill.session.{SessionConfig, SessionDirectives, SessionManager}
import com.softwaremill.sttp.SttpBackendOptions.connectionTimeout
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor.Transactor
import pureconfig.generic.auto._
import pureconfig.loadConfig
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepoImpl, DoobieConfig, UserRepoImpl}
import tech.cryptonomic.cloud.nautilus.routes.endpoint.Docs
import tech.cryptonomic.cloud.nautilus.routes.{ApiKeyRoutes, UserRoutes}
import tech.cryptonomic.cloud.nautilus.security.Provider.Github
import tech.cryptonomic.cloud.nautilus.security.{AuthProviderConfig, OauthService, Provider, Session, SttpOauthRepository}
import tech.cryptonomic.cloud.nautilus.services.{ApiKeyServiceImpl, UserServiceImpl}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Nautilus extends App with StrictLogging {

  lazy val githubConfig = loadConfig[AuthProviderConfig](namespace = "security.github.auth").toOption.get
  lazy val doobieConfig = loadConfig[DoobieConfig](namespace = "doobie").toOption.get
  lazy val xa = Transactor.fromDriverManager[IO](doobieConfig.driver, doobieConfig.url, doobieConfig.user, doobieConfig.password)

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val sttpBackend = AsyncHttpClientCatsBackend[IO](connectionTimeout(githubConfig.connectionTimeout.milliseconds))

  lazy val apiKeysRepo = new ApiKeyRepoImpl(xa)
  lazy val apiKeysService = new ApiKeyServiceImpl[IO](apiKeysRepo)
  lazy val apiKeysRoutes = new ApiKeyRoutes(apiKeysService)
  lazy val userRepo = new UserRepoImpl(xa)
  lazy val userService = new UserServiceImpl[IO](userRepo, apiKeysRepo)
  lazy val userRoutes = new UserRoutes(userService)
  lazy val githubOauthRepository = new SttpOauthRepository[IO](githubConfig)
  lazy val oauthService = new OauthService[IO](githubConfig, githubOauthRepository)

  implicit val sessionManager = new SessionManager[Session](SessionConfig.fromConfig())

  val requiredSession = SessionDirectives.requiredSession(oneOff, usingCookies)
  val invalidateSession = SessionDirectives.invalidateSession(oneOff, usingCookies)

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
      parameters('code) { code =>
        onComplete(oauthService.resolveAuthCode(code).unsafeToFuture()) {
          case Success(Right(email)) =>
            setSession(oneOff, usingCookies, Session(Github, email)) { ctx =>
              ctx.redirect("/", SeeOther)
            }
          case Failure(exception) =>
            logger.debug(exception.getMessage, exception)
            complete(Unauthorized)
          case Success(Left(exception)) =>
            logger.debug(exception.getMessage, exception)
            complete(Unauthorized)
        }
      }
    },
    // @TODO should be removed when a proper login page is created
    pathPrefix("site") {
      getFromResource("web/index.html")
    },
    requiredSession { session =>
      List(
        apiKeysRoutes.routes,
        userRoutes.routes,
        path("logout") {
          post {
            invalidateSession {
              complete(NoContent)
            }
          }
        },
        path("current_login") {
          get {
            complete(session.email)
          }
        }
      ).reduce(_ ~ _)
    }
  ).reduce(_ ~ _)

  Http().bindAndHandle(route, "0.0.0.0", 1234)
}
