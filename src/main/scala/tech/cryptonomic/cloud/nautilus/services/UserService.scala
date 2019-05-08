package tech.cryptonomic.cloud.nautilus.services

import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserWithoutId}

import scala.language.higherKinds

/** User service */
trait UserService[F[_]] {

  /** Creates user */
  def createUser(userWithoutId: UserWithoutId): F[Unit]

  /** Updated user */
  def updateUser(user: User): F[Unit]

  /** Returns user with given ID */
  def getUser(userId: Int): F[Option[User]]

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: Int): F[List[ApiKey]]
}
