package tech.cryptonomic.nautilus.cloud.application.domain.apiKey

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.application.domain.tier.{Tier, TierRepository, Usage}
import tech.cryptonomic.nautilus.cloud.application.domain.tools.ClockTool.ExtendedClock
import tech.cryptonomic.nautilus.cloud.application.domain.user.User.UserId

import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyService[F[_]: Monad](
    apiKeyRepository: ApiKeyRepository[F],
    tiersRepository: TierRepository[F],
    clock: Clock[F],
    apiKeyGenerator: ApiKeyGenerator
) {

  /** Returns all API keys from the DB */
  def getAllApiKeys: F[List[ApiKey]] = apiKeyRepository.getAllApiKeys

  /** Returns API Keys usage for current user with given ID */
  def getCurrentUserApiKeysUsage(userId: UserId): F[List[UsageLeft]] =
    apiKeyRepository.getKeysUsageForUser(userId)

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: UserId): F[List[ApiKey]] =
    apiKeyRepository.getUserApiKeys(userId)

  /** Returns API Keys usage for user with given ID */
  def getApiKeysUsage(userId: UserId): F[List[UsageLeft]] =
    apiKeyRepository.getKeysUsageForUser(userId)

  /** Returns all API keys from the DB */
  def getActiveApiKeys(userId: UserId): F[List[ApiKey]] =
    apiKeyRepository.getCurrentActiveApiKeys(userId)

  /** Returns all API keys from the DB */
  def getAllApiKeysForEnv(env: String): F[List[String]] =
    apiKeyRepository.getKeysForEnv(env)

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean] =
    apiKeyRepository.validateApiKey(apiKey)

  /** Refreshes api key */
  def refreshApiKey(userId: UserId, environment: Environment): F[Unit] =
    for {
      now <- clock.currentInstant
      _ <- apiKeyRepository.updateApiKey(RefreshApiKey(userId, environment, apiKeyGenerator.generateKey, now))
    } yield ()

  /** Initialize api keys for the newly created user */
  def initializeApiKeys(userId: UserId, usage: Usage): F[Unit] =
    for {
      now <- clock.currentInstant
      defaultTier <- tiersRepository.getDefault
      apiKeys = createApiKeys(userId, now, defaultTier)
      initialUsages = createInitialUsages(apiKeys, usage)
      _ <- insert(apiKeys, initialUsages)
    } yield ()

  private def insert(apiKeys: List[CreateApiKey], initialUsages: List[UsageLeft]): F[Unit] =
    for {
      _ <- apiKeys.traverse(apiKeyRepository.putApiKey)
      _ <- initialUsages.traverse(apiKeyRepository.putApiKeyUsage)
    } yield ()

  private def createApiKeys(userId: UserId, now: Instant, defaultTier: Tier) =
    Environment.all.map { environment =>
      CreateApiKey(
        key = apiKeyGenerator.generateKey,
        environment = environment,
        userId = userId,
        dateIssued = now,
        dateSuspended = None
      )
    }

  private def createInitialUsages(apiKeys: List[CreateApiKey], currentUsage: Usage) =
    apiKeys.map { apiKey =>
      UsageLeft(apiKey.key, currentUsage)
    }
}
