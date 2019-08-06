package tech.cryptonomic.nautilus.cloud.domain.user

import cats.Applicative
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** User service implementation */
class UserService[F[_]: Applicative](
    userRepo: UserRepository[F],
    apiKeyRepo: ApiKeyRepository[F]
) {

  /** Get current user */
  def getUserByEmailAddress(email: String): F[Option[User]] = userRepo.getUserByEmailAddress(email)

  /** Updated user */
  def updateUser(id: UserId, user: UpdateUser): F[Unit] = userRepo.updateUser(id, user)

  /** Returns user with given ID */
  def getUser(userId: UserId): F[Option[User]] = userRepo.getUser(userId)

  /** Returns API Keys for current user with given ID */
  def getCurrentUserApiKeys(userId: UserId): F[List[ApiKey]] = apiKeyRepo.getCurrentActiveApiKeys(userId)

  /** Returns API Keys usage for current user with given ID */
  def getCurrentUserApiKeysUsage(userId: UserId): F[List[UsageLeft]] = apiKeyRepo.getKeysUsageForUser(userId)

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: UserId): F[List[ApiKey]] = apiKeyRepo.getUserApiKeys(userId)

  /** Returns API Keys usage for user with given ID */
  def getUserApiKeysUsage(userId: UserId): F[List[UsageLeft]] = apiKeyRepo.getKeysUsageForUser(userId)
}
