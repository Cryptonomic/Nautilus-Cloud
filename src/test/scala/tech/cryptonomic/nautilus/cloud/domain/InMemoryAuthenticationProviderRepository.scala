package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.{AccessToken, Code, Email, Result}

import scala.collection.mutable

class InMemoryAuthenticationProviderRepository extends AuthenticationProviderRepository[Id] {

  private val availableAuthentications = new mutable.MutableList[(Code, AccessToken, Email)]

  def addMapping(code: Code, accessToken: AccessToken, email: Email): Unit = this.synchronized {
    availableAuthentications += ((code, accessToken, email))
  }

  def clear(): Unit = this.synchronized {
    availableAuthentications.clear()
  }

  override def exchangeCodeForAccessToken(code: Code): Result[AccessToken] = this.synchronized {
    availableAuthentications.collectFirst {
      case (`code`, accessToken, _) => accessToken
    }.toRight(new RuntimeException)
  }

  override def fetchEmail(accessToken: AccessToken): Result[Email] = this.synchronized {
    availableAuthentications.collectFirst {
      case (_, `accessToken`, email) => email
    }.toRight(new RuntimeException)
  }
}
