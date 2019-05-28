package tech.cryptonomic.nautilus.cloud.services

import tech.cryptonomic.nautilus.cloud.repositories.{ApiKeyRepo, UserRepo}
import tech.cryptonomic.nautilus.cloud.model.{ApiKey, User, UserWithoutId}

import scala.language.higherKinds

/** User service implementation */
class UserServiceImpl[F[_]](userRepo: UserRepo[F], apiKeyRepo: ApiKeyRepo[F]) extends UserService[F] {

  /** Creates user */
  override def createUser(userWithoutId: UserWithoutId): F[Int] =
    userRepo.createUser(userWithoutId)

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
