package tech.cryptonomic.nautilus.cloud.adapters.metering

import java.time.Instant

import cats.Monad
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, ApiKeyStats}
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApiRepository
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.{AggregatedMeteringStats, MeteringStatsRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.{User, UserRepository}

import scala.language.higherKinds

/** Gathers and processes metering data */
class MeteringProcessor[F[_]: Monad](
    meteringStatsRepository: MeteringStatsRepository[F],
    meteringApiRepository: MeteringApiRepository[F],
    apiKeyRepository: ApiKeyRepository[F],
    userRepository: UserRepository[F],
    meteringApiConfig: MeteringApiConfig
) extends StrictLogging {

  /** Process for getting current users, fetching stats, aggregating and inserting to the DB */
  def process(): F[Unit] =
    for {
      users <- userRepository.getUsers()().map(_.result)
      _ = logger.debug(s"Got users from DB: $users")
      meteringStats <- meteringStatsRepository.getLastStats(users.map(_.userId))
      _ = logger.debug(s"Got last stats from DB: $meteringStats")
      validApiKeys <- users.map { user =>
        val periodStart = meteringStats
          .find(_.userId == user.userId)
          .map(_.periodStart)
          .getOrElse(Instant.ofEpochSecond(0))
        apiKeyRepository
          .getUserActiveKeysInGivenRange(user.userId, periodStart, Instant.now())
      }.sequence
      _ = logger.debug(s"Valid API keys: ${validApiKeys.flatten}")
      apiKeyStats <- if (validApiKeys.flatten.isEmpty) {
        List.empty[ApiKeyStats].pure[F]
      } else {
        fetchApiKeyStats(validApiKeys.flatten, meteringStats)
      }
      aggregatedStats = aggregateStats(users, validApiKeys.flatten, apiKeyStats)
      _ = logger.debug(s"Inserting stats $aggregatedStats")
      _ <- meteringStatsRepository.insertStats(aggregatedStats)
      _ = logger.info(s"Successfully inserted ${aggregatedStats.size} aggregated stats")
    } yield ()

  private def aggregateStats(
      users: List[User],
      apiKeys: List[ApiKey],
      apiKeyStats: List[ApiKeyStats]
  ): List[AggregatedMeteringStats] =
    for {
      stats <- apiKeyStats
      user <- users
      apiKey <- apiKeys
      key <- stats.apiKey.toList
      if key == apiKey.key && apiKey.userId == user.userId
    } yield
      AggregatedMeteringStats(
        userId = user.userId,
        environment = apiKey.environment.name,
        hits = stats.count,
        periodStart = stats.time.minusSeconds(meteringApiConfig.statsInterval.toSeconds),
        periodEnd = stats.time
      )

  private def fetchApiKeyStats(
      validApiKeys: List[ApiKey],
      meteringStats: List[AggregatedMeteringStats]
  ): F[List[ApiKeyStats]] =
    meteringApiRepository
      .getApiKey5mStats(validApiKeys, meteringStats.map(_.periodEnd.toEpochMilli).minimumOption.map(_ / 1000))
      .map {
        case Left(e) =>
          logger.error(e.getMessage, e)
          throw e
        case Right(result) =>
          logger.debug(s"Got metering stats for aggregation: $result")
          result
      }

}
