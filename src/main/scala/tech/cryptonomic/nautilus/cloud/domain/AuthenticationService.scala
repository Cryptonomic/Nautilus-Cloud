package tech.cryptonomic.nautilus.cloud.domain

import akka.http.scaladsl.model.Uri
import cats.Monad
import cats.data.EitherT
import tech.cryptonomic.nautilus.cloud.domain.authentication.{
  AuthenticationConfiguration,
  AuthenticationProviderRepository
}

import scala.language.higherKinds

class AuthenticationService[F[_]: Monad](
    config: AuthenticationConfiguration,
    repository: AuthenticationProviderRepository[F]
) {

  type Result[T] = Either[Throwable, T]

  def loginUrl: Uri = config.loginUrl

  def resolveAuthCode(code: String): F[Result[String]] =
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .value

  private def exchangeCodeForAccessToken(code: String): EitherT[F, Throwable, String] =
    EitherT(repository.exchangeCodeForAccessToken(code))

  private def fetchEmail(accessToken: String): EitherT[F, Throwable, String] =
    EitherT(repository.fetchEmail(accessToken))
}
