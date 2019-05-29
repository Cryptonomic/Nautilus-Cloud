package tech.cryptonomic.cloud.nautilus.security

import scala.language.higherKinds

trait OauthRepository[F[_]] {

  type Result[T] = Either[Throwable, T]

  def exchangeCodeForAccessToken(code: String): F[Result[String]]

  def fetchEmail(accessToken: String): F[Result[String]]
}
