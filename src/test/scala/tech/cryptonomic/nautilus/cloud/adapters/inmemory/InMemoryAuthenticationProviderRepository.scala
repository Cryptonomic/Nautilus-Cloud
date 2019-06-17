package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Monad
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.AccessToken
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Code
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Result

import scala.language.higherKinds

class InMemoryAuthenticationProviderRepository[F[_]](implicit monad: Monad[F])
    extends AuthenticationProviderRepository[F] {

  private var availableAuthentications: List[(Code, AccessToken, Email)] = List.empty

  def addMapping(code: Code, accessToken: AccessToken, email: Email): Unit = this.synchronized {
    availableAuthentications = availableAuthentications :+ (code, accessToken, email)
  }

  def clear(): Unit = this.synchronized {
    availableAuthentications = List.empty
  }

  override def exchangeCodeForAccessToken(code: Code): F[Result[AccessToken]] = this.synchronized {
    monad.pure(
      availableAuthentications.collectFirst {
        case (`code`, accessToken, _) => accessToken
      }.toRight(new RuntimeException)
    )
  }

  override def fetchEmail(accessToken: AccessToken): F[Result[Email]] = this.synchronized {
    monad.pure(
      availableAuthentications.collectFirst {
        case (_, `accessToken`, email) => email
      }.toRight(new RuntimeException)
    )
  }
}
