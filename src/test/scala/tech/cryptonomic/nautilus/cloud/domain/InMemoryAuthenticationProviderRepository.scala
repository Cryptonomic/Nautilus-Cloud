package tech.cryptonomic.nautilus.cloud.domain

import cats.Monad
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.{
  AccessToken,
  Code,
  Email,
  Result
}

import scala.collection.mutable
import scala.language.higherKinds

class InMemoryAuthenticationProviderRepository[F[_]](implicit monad: Monad[F])
    extends AuthenticationProviderRepository[F] {

  private val availableAuthentications = new mutable.MutableList[(Code, AccessToken, Email)]

  def addMapping(code: Code, accessToken: AccessToken, email: Email): Unit = this.synchronized {
    availableAuthentications += ((code, accessToken, email))
  }

  def clear(): Unit = this.synchronized {
    availableAuthentications.clear()
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
