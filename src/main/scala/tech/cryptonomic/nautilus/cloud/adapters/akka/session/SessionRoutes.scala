package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import akka.http.scaladsl.model.StatusCodes.{Found, NoContent, SeeOther}
import akka.http.scaladsl.server.Directives.{complete, get, onComplete, parameters, path, post, redirect, reject, _}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.domain.AuthenticationService

import scala.util.{Failure, Success}

class SessionRoutes(
    private val authService: AuthenticationService[IO],
    private val sessionOperations: SessionOperations
) extends StrictLogging {

  lazy val routes: Route =
    List(
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
      sessionOperations.requiredSession { session =>
        List(
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
}
