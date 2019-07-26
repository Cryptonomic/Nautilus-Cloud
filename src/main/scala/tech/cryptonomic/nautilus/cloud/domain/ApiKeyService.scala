package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, requiredRole}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.resources.ResourceRepository
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierRepository, Usage}
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.concurrent.duration.MILLISECONDS
import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyService[F[_]: Monad](
    apiKeyRepository: ApiKeyRepository[F],
    resourcesRepository: ResourceRepository[F],
    tiersRepository: TierRepository[F],
    clock: Clock[F],
    apiKeyGenerator: ApiKeyGenerator
) {

  /** Returns all API keys from the DB */
  def getAllApiKeys(implicit session: Session): F[Permission[List[ApiKey]]] = requiredRole(Role.Administrator) {
    apiKeyRepository.getAllApiKeys
  }

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean] =
    apiKeyRepository.validateApiKey(apiKey)

  def initializeApiKeys(userId: UserId): F[Unit] = for {
      now <- clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)
      defaultTier <- tiersRepository.getDefault
      apiKeys = createApiKeys(userId, now, defaultTier)
      initialUsages = createInitialUsages(apiKeys, defaultTier.getCurrentUsage(now))
      _ <- insert(apiKeys, initialUsages)
    } yield ()

  private def insert(apiKeys: List[CreateApiKey], initialUsages: List[UsageLeft]): F[Unit] = for {
      _ <- apiKeys.traverse(apiKeyRepository.putApiKey)
      _ <- initialUsages.traverse(apiKeyRepository.putApiKeyUsage)
    } yield ()

  private def createApiKeys(userId: UserId, now: Instant, defaultTier: Tier) =
    Environment.all.map { environment =>
      CreateApiKey(
        key = apiKeyGenerator.generateKey,
        environment = environment,
        userId = userId,
        tierId = defaultTier.tierId,
        dateIssued = now,
        dateSuspended = None
      )
    }

  private def createInitialUsages(apiKeys: List[CreateApiKey], currentUsage: Usage) =
    apiKeys.map { apiKey =>
      UsageLeft(apiKey.key, currentUsage)
    }
}
