package tech.cryptonomic.nautilus.cloud.domain.authentication

import cats.Applicative
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.user.Role

import scala.language.higherKinds

object AuthorizationService {
  type Permission[T] = Either[AccessDenied, T]

  def requiredRole[F[_]: Applicative, T](
      requiredRole: Role
  )(f: => F[T])(implicit session: Session): F[Permission[T]] =
    if (requiredRole == session.role)
      f.map(Right(_))
    else
      AccessDenied(requiredRole, session).asLeft[T].pure[F]

  def requiredRoleOrApiKey[F[_]: Applicative, T](
      requiredRole: Role,
      apiKey: Option[String],
      supportedKeys: Set[String]
  )(f: => F[T])(implicit session: Session): F[Permission[T]] =
    if (requiredRole == session.role || apiKey.exists(supportedKeys))
      f.map(Right(_))
    else
      AccessDenied(requiredRole, session).asLeft[T].pure[F]

  def requiredApiKey[F[_]: Applicative, T](
      apiKey: String,
      supportedKeys: Set[String]
  )(f: => F[T]): F[Permission[T]] =
    Either
      .cond(
        supportedKeys(apiKey),
        f,
        AccessDenied("Wrong API key").pure[F]
      )
      .bisequence
}
