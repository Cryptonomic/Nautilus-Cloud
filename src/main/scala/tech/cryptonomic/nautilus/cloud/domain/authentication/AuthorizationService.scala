package tech.cryptonomic.nautilus.cloud.domain.authentication

import cats.Applicative
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.user.Role

import scala.language.higherKinds

object AuthorizationService {
  type Permission[T] = Either[PermissionDenied, T]

  def requiredRole[F[_]: Applicative, T](
      requiredRole: Role
  )(f: => F[T])(implicit session: Session): F[Permission[T]] =
    if (requiredRole == session.role)
      f.map(Right(_))
    else
      (Left(PermissionDenied(requiredRole, session.role)): Permission[T]).pure[F]
}
