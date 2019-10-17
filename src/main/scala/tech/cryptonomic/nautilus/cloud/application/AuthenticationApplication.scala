package tech.cryptonomic.nautilus.cloud.application

import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Result
import tech.cryptonomic.nautilus.cloud.domain.authentication.RegistrationAttempt.RegistrationAttemptId
import tech.cryptonomic.nautilus.cloud.domain.authentication.{ConfirmRegistration, AuthenticationService, Session}
import tech.cryptonomic.nautilus.cloud.domain.user.User

import scala.language.higherKinds

/** Authentication service */
class AuthenticationApplication[F[_]](
    authenticationService: AuthenticationService[F]
) {

  /* return login url for authentication */
  def loginUrl: String = authenticationService.loginUrl

  /* resolve auth code */
  def resolveAuthCode(code: String): F[Result[Either[RegistrationAttemptId, User]]] =
    authenticationService.resolveAuthCode(code)

  /* accept registration */
  def acceptRegistration(acceptRegistration: ConfirmRegistration): F[Result[User]] =
    authenticationService.acceptRegistration(acceptRegistration)
}
