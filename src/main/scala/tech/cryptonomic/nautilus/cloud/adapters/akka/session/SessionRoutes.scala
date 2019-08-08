package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import akka.http.scaladsl.model.StatusCodes.{Found, NoContent, SeeOther}
import akka.http.scaladsl.server.Directives.{complete, onComplete, parameters, path, post, redirect, reject, _}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.application.AuthenticationApplication

import scala.util.{Failure, Success}

class SessionRoutes(
    private val authenticationApplication: AuthenticationApplication[IO],
    private val sessionOperations: SessionOperations
) extends StrictLogging {

  lazy val routes: Route =
    concat(
      path("github-login") {
        redirect(authenticationApplication.loginUrl, Found)
      },
      path("users" / "github-init") {
        parameters('code) {
          code =>
            onComplete(authenticationApplication.resolveAuthCode(code).unsafeToFuture()) {
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
