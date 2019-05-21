package tech.cryptonomic.cloud.nautilus.domain

import cats.Monad
import cats.data.EitherT
import tech.cryptonomic.cloud.nautilus.adapters.sttp.GithubConfig
import tech.cryptonomic.cloud.nautilus.domain.security.GithubRepository

import scala.language.higherKinds

class SecurityService[F[_]](config: GithubConfig, repository: GithubRepository[F])(implicit monad: Monad[F]) {

  type Result[T] = Either[Throwable, T]

  val scopes = List("user:email")

  def loginUrl: String = config.loginUrl + s"?scope=${scopes.mkString(",")}&client_id=" + config.clientId

  def resolveAuthCode(code: String): F[Result[String]] =
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .value

  private def exchangeCodeForAccessToken(code: String): EitherT[F, Throwable, String] =
    EitherT(repository.exchangeCodeForAccessToken(code))

  private def fetchEmail(accessToken: String): EitherT[F, Throwable, String] =
    EitherT(repository.fetchEmail(accessToken))
}
