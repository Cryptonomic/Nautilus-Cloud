package tech.cryptonomic.cloud.nautilus.repositories

import scala.language.higherKinds

import tech.cryptonomic.cloud.nautilus.model.{User, UserWithoutId}

/** Trait representing User repo queries */
trait UserRepo[F[_]] {

  /** Creates user */
  def createUser(userReg: UserWithoutId): F[Unit]

  /** Updates user */
  def updateUser(user: User): F[Unit]

  /** Returns user */
  def getUser(userId: Int): F[Option[User]]
}
