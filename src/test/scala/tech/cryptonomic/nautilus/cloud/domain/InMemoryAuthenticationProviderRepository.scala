package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.{
  AccessToken,
  Code,
  Email,
  Result
}

class InMemoryAuthenticationProviderRepository(availableAuthentications: List[(Code, AccessToken, Email)])
    extends AuthenticationProviderRepository[Id] {

  override def exchangeCodeForAccessToken(code: Code): Result[AccessToken] =
    availableAuthentications.collectFirst {
      case (`code`, accessToken, _) => accessToken
    }.toRight(new RuntimeException)

  override def fetchEmail(accessToken: AccessToken): Result[Email] =
    availableAuthentications.collectFirst {
      case (_, `accessToken`, email) => email
    }.toRight(new RuntimeException)
}
