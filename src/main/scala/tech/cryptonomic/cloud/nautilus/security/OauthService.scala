package tech.cryptonomic.cloud.nautilus.security

import cats.Monad
import cats.data.EitherT

import scala.language.higherKinds

class OauthService[F[_]](config: AuthProviderConfig, repository: OauthRepository[F])(implicit monad: Monad[F]) {

  type Result[T] = Either[Throwable, T]

  val scopes = List("user:email")

  def loginUrl: String = config.loginUrl + s"?scope=${scopes.mkString(",")}&client_id=" + config.clientId

  def resolveAuthCode(code: String): F[Result[String]] = {
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .value
  }

  private def exchangeCodeForAccessToken(code: String): EitherT[F, Throwable, String] =
    EitherT(repository.exchangeCodeForAccessToken(code))

  private def fetchEmail(accessToken: String): EitherT[F, Throwable, String] =
    EitherT(repository.fetchEmail(accessToken))
}
