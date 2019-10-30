package tech.cryptonomic.nautilus.cloud.application

import cats.Applicative
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, _}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.pagination.{PaginatedResult, Pagination}
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{
  AdminUpdateUser,
  Role,
  UpdateCurrentUser,
  UpdateUser,
  User,
  UserService
}

import scala.language.higherKinds

/** User service implementation */
class UserApplication[F[_]: Applicative](
    userService: UserService[F]
) {

  /** Get current user */
  def getCurrentUser(implicit session: Session): F[Option[User]] = userService.getUserByEmailAddress(session.email)

  /** Get users */
  def getUsers(userId: Option[UserId] = None, email: Option[Email] = None, apiKey: Option[String] = None)(
      pagination: Pagination
  )(
      implicit session: Session
  ): F[Permission[PaginatedResult[User]]] = requiredRole(Administrator) {
    userService.getUsers(userId, email, apiKey)(pagination)
  }

  /** Update user */
  def updateUser(id: UserId, user: AdminUpdateUser)(implicit session: Session): F[Permission[Unit]] =
    requiredRole(Administrator) {
      userService.updateUser(id, user.updateUser)
    }

  /** Update current user */
  def updateCurrentUser(user: UpdateCurrentUser)(implicit session: Session): F[Unit] =
    userService.updateUser(session.userId, user.updateUser)

  /** Delete current user */
  def deleteCurrentUser(implicit session: Session): F[Permission[Unit]] = requiredRole(Role.User) {
    userService.deleteUser(session.userId)
  }

  /** Delete user */
  def deleteUser(id: UserId)(implicit session: Session): F[Permission[Unit]] = requiredRole(Administrator) {
    userService.deleteUser(id)
  }

  /** Return user with a given ID */
  def getUser(userId: UserId)(implicit session: Session): F[Permission[Option[User]]] = requiredRole(Administrator) {
    userService.getUser(userId)
  }
}
