package tech.cryptonomic.nautilus.cloud.domain

import cats.Applicative
import cats.implicits._
import cats.syntax.applicative._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.AccessToken
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Code
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Result

import scala.collection.mutable
import scala.language.higherKinds

class InMemoryAuthenticationProviderRepository[F[_]: Applicative] extends AuthenticationProviderRepository[F] {

  private val availableAuthentications = new mutable.MutableList[(Code, AccessToken, Email)]

  def addMapping(code: Code, accessToken: AccessToken, email: Email): Unit = this.synchronized {
    availableAuthentications += ((code, accessToken, email))
  }

  def clear(): Unit = this.synchronized {
    availableAuthentications.clear()
  }

  override def exchangeCodeForAccessToken(code: Code): F[Result[AccessToken]] = this.synchronized {
    availableAuthentications.collectFirst {
      case (`code`, accessToken, _) => accessToken
    }.toRight(new RuntimeException).pure
  }

  override def fetchEmail(accessToken: AccessToken): F[Result[Email]] = this.synchronized {
    availableAuthentications.collectFirst {
      case (_, `accessToken`, email) => email
    }.toRight(new RuntimeException).pure
  }
}
