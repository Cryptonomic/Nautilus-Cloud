package tech.cryptonomic.nautilus.cloud.application

import cats.Applicative
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, _}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{Role, UpdateUser, User, UserService}

import scala.language.higherKinds

/** User service implementation */
class UserApplication[F[_]: Applicative](
    userService: UserService[F]
) {

  /** Get current user */
  def getCurrentUser(implicit session: Session): F[Option[User]] = userService.getUserByEmailAddress(session.email)

  /** Update user */
  def updateUser(id: UserId, user: UpdateUser)(implicit session: Session): F[Permission[Unit]] =
    requiredRole(Administrator) {
      userService.updateUser(id, user)
    }

  /** Delete current user */
  def deleteCurrentUser(implicit session: Session): F[Permission[Unit]] = requiredRole(Role.User) {
    userService.deleteUser(session.userId)
  }

  /** Return user with a given ID */
  def getUser(userId: UserId)(implicit session: Session): F[Permission[Option[User]]] = requiredRole(Administrator) {
    userService.getUser(userId)
  }
}
