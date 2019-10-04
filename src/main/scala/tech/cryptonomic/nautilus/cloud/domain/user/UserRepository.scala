package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.pagination.{PaginatedResult, Pagination}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** Trait representing User repo queries */
trait UserRepository[F[_]] {

  /** Creates user */
  def createUser(user: CreateUser): F[Either[Throwable, UserId]]

  /** Updates user */
  def updateUser(id: UserId, user: UpdateUser): F[Unit]

  /** Delete user */
  def deleteUser(id: UserId, now: Instant): F[Unit]

  /** Returns user */
  def getUser(id: UserId): F[Option[User]]

  /** Returns user by email address */
  def getUserByEmailAddress(email: String): F[Option[User]]

  /** Returns all users */
  def getUsers(userId: Option[UserId] = None, email: Option[Email] = None, apiKey: Option[String] = None)(
      pagination: Pagination = Pagination.allResults
  ): F[PaginatedResult[User]]
}
