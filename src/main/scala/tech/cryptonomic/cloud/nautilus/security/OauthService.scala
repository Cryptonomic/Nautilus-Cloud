package tech.cryptonomic.cloud.nautilus.security

import scala.language.higherKinds

trait OauthService[F[_]] {
  def resolveAuthCode(code: String): F[Either[Throwable, String]]
}
