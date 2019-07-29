package tech.cryptonomic.nautilus.cloud.application

import cats.Applicative
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.AuthorizationService.{Permission, _}
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.application.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.application.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.application.domain.user.{UpdateUser, User, UserService}

import scala.language.higherKinds

/** User service implementation */
class UserApplication[F[_]: Applicative](
    userService: UserService[F]
) {

  /** Get current user */
  def getCurrentUser(implicit session: Session): F[Option[User]] = userService.getUserByEmailAddress(session.email)

  /** Updated user */
  def updateUser(id: UserId, user: UpdateUser)(implicit session: Session): F[Permission[Unit]] =
    requiredRole(Administrator) {
      userService.updateUser(id, user)
    }

  /** Returns user with given ID */
  def getUser(userId: UserId)(implicit session: Session): F[Permission[Option[User]]] = requiredRole(Administrator) {
    userService.getUser(userId)
  }
}
