package tech.cryptonomic.cloud.nautilus.services

import cats.effect.IO
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserReg}
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepo, UserRepo}

import scala.concurrent.Future

trait UserService {

  def createUser(userReg: UserReg): Future[Unit]

  def updateUser(user: User): Future[Unit]

  def getUser(userId: Long): Future[Option[User]]

  def getUserApiKeys(userId: Long): Future[List[ApiKey]]
}

class UserServiceImpl(userRepo: UserRepo[IO], apiKeyRepo: ApiKeyRepo[IO]) extends UserService {
  override def createUser(userReg: UserReg): Future[Unit] =
    userRepo.createUser(userReg).map(_ => ()).unsafeToFuture()

  override def updateUser(user: User): Future[Unit] =
    userRepo.updateUser(user).map(_ => ()).unsafeToFuture()

  override def getUser(userId: Long): Future[Option[User]] =
    userRepo.getUser(userId).unsafeToFuture()

  override def getUserApiKeys(userId: Long): Future[List[ApiKey]] =
    apiKeyRepo.getUserApiKeys(userId).unsafeToFuture()
}
