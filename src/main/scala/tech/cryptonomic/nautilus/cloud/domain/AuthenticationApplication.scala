package tech.cryptonomic.nautilus.cloud.domain

import cats.Monad
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Result
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AuthenticationService, Session}

import scala.language.higherKinds

/** Authentication service */
class AuthenticationApplication[F[_]: Monad](
    authenticationService: AuthenticationService[F]
) {

  /* return login url for authentication */
  def loginUrl: String = authenticationService.loginUrl

  /* resolve auth code */
  def resolveAuthCode(code: String): F[Result[Session]] = authenticationService.resolveAuthCode(code)
}
