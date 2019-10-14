package tech.cryptonomic.nautilus.cloud.domain.user

import cats.Monad
import cats.implicits._
import cats.effect.Clock
import tech.cryptonomic.nautilus.cloud.adapters.doobie.SearchCriteria
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.pagination.{PaginatedResult, Pagination}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.tools.ClockTool.ExtendedClock

import scala.language.higherKinds

/** User service implementation */
class UserService[F[_]: Monad](
    userRepo: UserRepository[F],
    apiKeyRepo: ApiKeyRepository[F],
    clock: Clock[F]
) {

  def deleteUser(userId: UserId): F[Unit] =
    for {
      now <- clock.currentInstant
      _ <- apiKeyRepo.invalidateApiKeys(userId, now)
      _ <- userRepo.deleteUser(userId, now)
    } yield ()

  /** Get user by email address */
  def getUserByEmailAddress(email: String): F[Option[User]] = userRepo.getUserByEmailAddress(email)

  /** Get current user */
  def getUsers(userId: Option[UserId], email: Option[Email], apiKey: Option[String])(
      pagination: Pagination
  ): F[PaginatedResult[User]] =
    userRepo.getUsers(SearchCriteria(userId, email, apiKey))(pagination)

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
