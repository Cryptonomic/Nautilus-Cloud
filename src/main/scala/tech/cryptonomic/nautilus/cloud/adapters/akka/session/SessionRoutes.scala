package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import akka.http.scaladsl.model.StatusCodes.{Found, NoContent}
import akka.http.scaladsl.server.Directives.{complete, onComplete, path, post, redirect, reject, _}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import tech.cryptonomic.nautilus.cloud.application.AuthenticationApplication
import tech.cryptonomic.nautilus.cloud.domain.authentication.{
  HeaderType,
  InitRequest,
  InitResponse,
  RegistrationAttemptResponse,
  RegistrationConfirmation,
  UserResponse
}

import scala.util.{Failure, Success}

class SessionRoutes(
    private val authenticationApplication: AuthenticationApplication[IO],
    private val sessionOperations: SessionOperations
) extends ErrorAccumulatingCirceSupport
    with StrictLogging {

  import tech.cryptonomic.nautilus.cloud.domain.authentication.InitResponseEncoders._

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
                    ctx.complete(InitResponse(HeaderType.REGISTERED, UserResponse(user)))
                  }
                case Success(Right(Left(registrationAttemptId))) =>
                  complete(InitResponse(HeaderType.REGISTRATION, RegistrationAttemptResponse(registrationAttemptId)))
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
      path("users" / "register") {
        post {
          // it may not work as expected because this service will be behind CF and proxy
          extractClientIP {
            ip =>
              entity(as[RegistrationConfirmation]) {
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
                      sessionOperations.setSession(
                        user.copy(tosAccepted = registrationAttemptRequest.tosAccepted).asSession
                      ) { ctx =>
                        ctx.complete(InitResponse(HeaderType.REGISTERED, UserResponse(user)))
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
