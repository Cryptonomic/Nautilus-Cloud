package tech.cryptonomic.nautilus.cloud.domain.apiKey

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApiRepository
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.{AggregatedMeteringStats, MeteringStatsRepository}
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierRepository, Usage}
import tech.cryptonomic.nautilus.cloud.domain.tools.ClockTool.ExtendedClock
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyService[F[_]: Monad](
    apiKeyRepository: ApiKeyRepository[F],
    tiersRepository: TierRepository[F],
    meteringApi: MeteringApiRepository[F],
    meteringStatsRepository: MeteringStatsRepository[F],
    clock: Clock[F],
    apiKeyGenerator: ApiKeyGenerator
) extends StrictLogging {

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
  def getAllApiKeysForEnv(environment: Environment): F[List[String]] =
    apiKeyRepository.getKeysForEnv(environment)

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean] =
    apiKeyRepository.validateApiKey(apiKey)

  /** Refreshes api key */
  def refreshApiKey(userId: UserId, environment: Environment): F[ApiKey] =
    for {
      now <- clock.currentInstant
      apiKey <- apiKeyRepository.updateApiKey(RefreshApiKey(userId, environment, apiKeyGenerator.generateKey, now))
    } yield apiKey

  /** Initialize api keys for the newly created user */
  def initializeApiKeys(userId: UserId, usage: Usage): F[Unit] =
    for {
      now <- clock.currentInstant
      defaultTier <- tiersRepository.getDefault
      apiKeys = createApiKeys(userId, now, defaultTier)
      initialUsages = createInitialUsages(apiKeys, usage)
      _ <- insert(apiKeys, initialUsages)
    } yield ()

  /** Returns stats for ApiKeyQueries */
  def getMeteringStats(userId: UserId): F[MeteringStats] =
    for {
      activeApiKeys <- apiKeyRepository.getUserActiveKeysForLastMonth(userId)
      meteringStats <- fetchMeteringStats(activeApiKeys)
    } yield meteringStats

  /** Returns aggregated stats for the user */
  def getAggregatedMeteringStatsForUser(userId: UserId): F[List[AggregatedMeteringStats]] =
    meteringStatsRepository.getStatsPerUser(userId)

  private def fetchMeteringStats(activeApiKeys: List[ApiKey]): F[MeteringStats] =
    (
      meteringApi.getApiKey5mStats(activeApiKeys),
      meteringApi.getApiKey24hStats(activeApiKeys),
      meteringApi.getRoute5mStats(activeApiKeys),
      meteringApi.getRoute24hStats(activeApiKeys),
      meteringApi.getIp5mStats(activeApiKeys),
      meteringApi.getIp24hStats(activeApiKeys)
    ).mapN {
      case (apiKeyStats5m, apiKeyStats24h, routeStats5m, routeStats24h, ipStats5m, ipStats24h) =>
        val res = for {
          a5m <- apiKeyStats5m
          a24h <- apiKeyStats24h
          r5m <- routeStats5m
          r24h <- routeStats24h
          i5m <- ipStats5m
          i24h <- ipStats24h
        } yield MeteringStats(a5m, a24h, r5m, r24h, i5m, i24h)

        res match {
          case Left(exception) =>
            logger.error(exception.getMessage, exception)
            throw exception
          case Right(value) =>
            logger.info(s"Fetched metering stats: $value")
            value
        }
    }

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
