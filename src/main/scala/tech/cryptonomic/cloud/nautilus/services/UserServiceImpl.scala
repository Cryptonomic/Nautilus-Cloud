package tech.cryptonomic.cloud.nautilus.services

import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserRegistration}
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepo, UserRepo}

import scala.language.higherKinds

/** User service implementation */
class UserServiceImpl[F[_]](userRepo: UserRepo[F], apiKeyRepo: ApiKeyRepo[F]) extends UserService[F] {

  /** Creates user */
  override def createUser(userReg: UserRegistration): F[Unit] =
    userRepo.createUser(userReg)

  /** Updated user */
  override def updateUser(user: User): F[Unit] =
    userRepo.updateUser(user)

  /** Returns user with given ID */
  override def getUser(userId: Int): F[Option[User]] =
    userRepo.getUser(userId)

  /** Returns API Keys for user with given ID */
  override def getUserApiKeys(userId: Int): F[List[ApiKey]] =
    apiKeyRepo.getUserApiKeys(userId)
}
