package tech.cryptonomic.nautilus.cloud

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{Found, NoContent, SeeOther}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.Docs
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator

import scala.util.{Failure, Success}

object NautilusCloud extends App with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val context = NautilusContext
  val authService = context.authService
  val sessionOperations = context.sessionOperations
  val apiKeysRoutes = context.apiKeysRoutes
  val userRoutes = context.userRoutes
  val httpConfig = context.httpConfig

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
      redirect(authService.loginUrl, Found)
    },
    path("github-callback") {
      parameters('code) {
        code =>
          onComplete(authService.resolveAuthCode(code).unsafeToFuture()) {
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

  logger.info("Nautilus Cloud started on {} at port {}", httpConfig.host, httpConfig.port)
  Http().bindAndHandle(route, httpConfig.host, httpConfig.port)
}
