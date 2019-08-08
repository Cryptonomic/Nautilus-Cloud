package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{Found, NoContent, SeeOther}
import akka.http.scaladsl.server.Directives.{complete, onComplete, path, post, redirect, reject, _}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import spray.json.DefaultJsonProtocol
import tech.cryptonomic.nautilus.cloud.application.AuthenticationApplication

import scala.util.{Failure, Success}

final case class InitRequest(code: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat1(InitRequest)
}

class SessionRoutes(
    private val authenticationApplication: AuthenticationApplication[IO],
    private val sessionOperations: SessionOperations
) extends JsonSupport with StrictLogging {

  lazy val routes: Route =
    concat(
      path("github-login") {
        redirect(authenticationApplication.loginUrl, Found)
      },
      path("users" / "github-init") {
        post {
          entity(as[InitRequest]) {
            code =>
              onComplete(authenticationApplication.resolveAuthCode(code.code).unsafeToFuture()) {
                case Success(Right(user)) =>
                  sessionOperations.setSession(user.asSession) { ctx =>
                    ctx.redirect("/users/me", SeeOther)
                  }
                case Failure(exception) =>
                  logger.error(exception.getMessage, exception)
                  reject(AuthorizationFailedRejection)
                case Success(Left(exception)) =>
                  logger.error(exception.getMessage, exception)
                  reject(AuthorizationFailedRejection)
              }
          }
        }
      },
      sessionOperations.requiredSession { session =>
        path("logout") {
          post {
            sessionOperations.invalidateSession {
              complete(NoContent)
            }
          }
        }
      }
    )
}
