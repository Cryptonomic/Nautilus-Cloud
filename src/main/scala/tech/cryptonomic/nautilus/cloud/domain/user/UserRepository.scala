package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

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
}
