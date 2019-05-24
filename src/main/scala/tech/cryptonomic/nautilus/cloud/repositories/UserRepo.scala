package tech.cryptonomic.nautilus.cloud.repositories

import scala.language.higherKinds
import tech.cryptonomic.nautilus.cloud.model.{User, UserWithoutId}

/** Trait representing User repo queries */
trait UserRepo[F[_]] {

  /** Creates user */
  def createUser(userReg: UserWithoutId): F[Int]

  /** Updates user */
  def updateUser(user: User): F[Unit]

  /** Returns user */
  def getUser(userId: Int): F[Option[User]]
}
