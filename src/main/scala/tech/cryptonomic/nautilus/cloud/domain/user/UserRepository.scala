package tech.cryptonomic.nautilus.cloud.domain.user

import scala.language.higherKinds

/** Trait representing User repo queries */
trait UserRepository[F[_]] {

  /** Creates user */
  def createUser(userReg: UserWithoutId): F[Int]

  /** Updates user */
  def updateUser(user: User): F[Unit]

  /** Returns user */
  def getUser(userId: Int): F[Option[User]]
}
