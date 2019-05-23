package tech.cryptonomic.nautilus.cloud.services

import tech.cryptonomic.nautilus.cloud.model.{ApiKey, User, UserWithoutId}

import scala.language.higherKinds

/** User service */
trait UserService[F[_]] {

  /** Creates user */
  def createUser(userWithoutId: UserWithoutId): F[Int]

  /** Updated user */
  def updateUser(user: User): F[Unit]

  /** Returns user with given ID */
  def getUser(userId: Int): F[Option[User]]

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: Int): F[List[ApiKey]]
}
