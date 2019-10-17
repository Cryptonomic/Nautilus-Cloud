package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import java.time.Instant

import akka.http.scaladsl.model.StatusCodes.{Found, NoContent}
import akka.http.scaladsl.server.Directives.{complete, onComplete, path, post, redirect, reject, _}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import tech.cryptonomic.nautilus.cloud.application.AuthenticationApplication
import tech.cryptonomic.nautilus.cloud.domain.authentication.ConfirmRegistration
import tech.cryptonomic.nautilus.cloud.domain.authentication.RegistrationAttempt.RegistrationAttemptId
import tech.cryptonomic.nautilus.cloud.domain.user.User
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.util.{Failure, Success}

final case class InitRequest(code: String)

final case class UserResponse(
    userId: UserId,
    userEmail: String,
    userRole: String,
    registrationDate: Instant,
    accountSource: String
)

final case class RegistrationAttemptResponse(
    registrationAttemptId: RegistrationAttemptId
)

object UserResponse {
  def apply(user: User): UserResponse =
    new UserResponse(user.userId, user.userEmail, user.userRole.name, user.registrationDate, user.accountSource.name)
}

class SessionRoutes(
    private val authenticationApplication: AuthenticationApplication[IO],
    private val sessionOperations: SessionOperations
) extends ErrorAccumulatingCirceSupport
    with StrictLogging {

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
                case Success(Right(Right(user))) =>
                  sessionOperations.setSession(user.asSession) { ctx =>
                    ctx.complete(UserResponse(user))
                  }
                case Success(Right(Left(registrationAttemptId))) =>
                  complete(RegistrationAttemptResponse(registrationAttemptId))
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
      path("users" / "accept-registration") {
        post {
          extractClientIP {
            ip =>
              entity(as[ConfirmRegistration]) {
                registrationAttemptRequest =>
                  onComplete(
                    authenticationApplication
                      .acceptRegistration(
                        registrationAttemptRequest.copy(
                          ipAddress = ip.toIP.map(_.ip.getHostAddress).orElse(registrationAttemptRequest.ipAddress)
                        )
                      )
                      .unsafeToFuture()
                  ) {
                    case Success(Right(user)) =>
                      sessionOperations.setSession(user.asSession) { ctx =>
                        ctx.complete(UserResponse(user))
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
