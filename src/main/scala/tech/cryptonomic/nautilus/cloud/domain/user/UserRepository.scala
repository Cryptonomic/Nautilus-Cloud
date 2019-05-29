package tech.cryptonomic.nautilus.cloud.domain.user

import scala.language.higherKinds

/** Trait representing User repo queries */
trait UserRepository[F[_]] {

  /** Creates user */
  def createUser(user: CreateUser): F[Either[Throwable, Int]]

  /** Updates user */
  def updateUser(id: Int, user: UpdateUser): F[Unit]

  /** Returns user */
  def getUser(id: Int): F[Option[User]]

  /** Returns user by email address */
  def getUserByEmailAddress(email: String): F[Option[User]]
}
