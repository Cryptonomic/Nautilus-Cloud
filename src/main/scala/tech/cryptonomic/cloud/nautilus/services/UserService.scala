package tech.cryptonomic.cloud.nautilus.services

import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserReg}

import scala.language.higherKinds

trait UserService[F[_]] {

  def createUser(userReg: UserReg): F[Unit]

  def updateUser(user: User): F[Unit]

  def getUser(userId: Int): F[Option[User]]

  def getUserApiKeys(userId: Int): F[List[ApiKey]]
}
