package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Applicative
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.AccessToken
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Code
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Result

import scala.language.higherKinds

class InMemoryAuthenticationProviderRepository[F[_]: Applicative] extends AuthenticationProviderRepository[F] {

  private var availableAuthentications: List[(Code, AccessToken, Email)] = List.empty

  def addMapping(code: Code, accessToken: AccessToken, email: Email): Unit = this.synchronized {
    availableAuthentications = availableAuthentications :+ (code, accessToken, email)
  }

  def clear(): Unit = this.synchronized {
    availableAuthentications = List.empty
  }

  override def exchangeCodeForAccessToken(code: Code): F[Result[AccessToken]] = this.synchronized {
    availableAuthentications.collectFirst {
      case (`code`, accessToken, _) => accessToken
    }.toRight(new Throwable).pure[F]
  }

  override def fetchEmail(accessToken: AccessToken): F[Result[Email]] = this.synchronized {
    availableAuthentications.collectFirst {
      case (_, `accessToken`, email) => email
    }.toRight(new Throwable).pure[F]
  }
}
