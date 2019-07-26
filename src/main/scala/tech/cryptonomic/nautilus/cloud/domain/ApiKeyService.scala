package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{requiredRole, Permission}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierRepository, Usage}
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.concurrent.duration.MILLISECONDS
import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyService[F[_]: Monad](
    apiKeyRepository: ApiKeyRepository[F],
    tiersRepository: TierRepository[F],
    clock: Clock[F],
    apiKeyGenerator: ApiKeyGenerator
) {

  /** Returns all API keys from the DB */
  def getAllApiKeys(implicit session: Session): F[Permission[List[ApiKey]]] = requiredRole(Role.Administrator) {
    apiKeyRepository.getAllApiKeys
  }

  /** Returns all API keys from the DB */
  def getCurrentActiveApiKeys(implicit session: Session): F[List[ApiKey]] = apiKeyRepository.getCurrentActiveApiKeys(session.id)

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean] =
    apiKeyRepository.validateApiKey(apiKey)

  def refreshApiKey(environment: Environment)(implicit session: Session): F[Unit] = currentInstant.flatMap { now =>
    apiKeyRepository.updateApiKey(RefreshApiKey(session.id, environment, apiKeyGenerator.generateKey, now))
  }

  /** Initialize api keys for the newly created user */
  def initializeApiKeys(userId: UserId, usage: Usage): F[Unit] =
    for {
      now <- currentInstant
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

  private def currentInstant = clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)
}
