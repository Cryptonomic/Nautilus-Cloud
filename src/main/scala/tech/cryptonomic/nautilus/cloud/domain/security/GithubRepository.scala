package tech.cryptonomic.nautilus.cloud.domain.security

import scala.language.higherKinds

trait GithubRepository[F[_]] {

  type Result[T] = Either[Throwable, T]

  type Code = String
  type AccessToken = String
  type Email = String

  def exchangeCodeForAccessToken(code: Code): F[Result[AccessToken]]

  def fetchEmail(accessToken: AccessToken): F[Result[Email]]
}
