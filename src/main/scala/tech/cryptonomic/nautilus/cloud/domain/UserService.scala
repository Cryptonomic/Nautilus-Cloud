package tech.cryptonomic.nautilus.cloud.domain

import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User, UserRepository}

import scala.language.higherKinds

/** User service implementation */
class UserService[F[_]](userRepo: UserRepository[F], apiKeyRepo: ApiKeyRepository[F]) {

  /** Creates user */
  def createUser(user: CreateUser): F[Either[Throwable, UserId]] =
    userRepo.createUser(user)

  /** Updated user */
  def updateUser(id: Int, user: UpdateUser): F[Unit] =
    userRepo.updateUser(id, user)

  /** Returns user with given ID */
  def getUser(userId: Int): F[Option[User]] =
    userRepo.getUser(userId)

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: Int): F[List[ApiKey]] =
    apiKeyRepo.getUserApiKeys(userId)

  def getUserApiKeysUsage(userId: Int): F[List[UsageLeft]] =
    apiKeyRepo.getKeysUsageForUser(userId)
}
