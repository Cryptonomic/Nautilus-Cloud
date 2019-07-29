package tech.cryptonomic.nautilus.cloud.application.domain.authentication

import tech.cryptonomic.nautilus.cloud.application.domain.authentication.AuthenticationProviderRepository.{
  AccessToken,
  Code,
  Email,
  Result
}

import scala.language.higherKinds

/* trait for authentication operations */
trait AuthenticationProviderRepository[F[_]] {
  /* exchange code for an access token */
  def exchangeCodeForAccessToken(code: Code): F[Result[AccessToken]]

  /* fetch an email using an access token */
  def fetchEmail(accessToken: AccessToken): F[Result[Email]]
}

object AuthenticationProviderRepository {
  type Result[T] = Either[Throwable, T]

  type Code = String
  type AccessToken = String
  type Email = String
}
