package tech.cryptonomic.nautilus.cloud.application

import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Result
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AuthenticationService, Session}
import tech.cryptonomic.nautilus.cloud.domain.user.User

import scala.language.higherKinds

/** Authentication service */
class AuthenticationApplication[F[_]](
    authenticationService: AuthenticationService[F]
) {

  /* return login url for authentication */
  def loginUrl: String = authenticationService.loginUrl

  /* resolve auth code */
  def resolveAuthCode(code: String): F[Result[User]] = authenticationService.resolveAuthCode(code)
}
