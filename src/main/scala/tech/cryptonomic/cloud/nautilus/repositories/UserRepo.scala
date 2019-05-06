package tech.cryptonomic.cloud.nautilus.repositories

import doobie.util.query.Query0
import scala.language.higherKinds

import tech.cryptonomic.cloud.nautilus.model.{User, UserRegistration}

/** Trait representing Doobie User repo queries */
trait UserRepo[F[_]] {
  /** Creates user */
  def createUser(userReg: UserRegistration): F[Unit]

  /** Updates user */
  def updateUser(user: User): F[Unit]

  /** Returns user */
  def getUser(userId: Int): F[Option[User]]
}
