package tech.cryptonomic.nautilus.cloud.application

import java.time.Instant

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.adapters.conseil.ConseilConfig
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{
  requiredApiKey,
  requiredRole,
  requiredRoleOrApiKey,
  Permission
}
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AccessDenied, Session}
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.AggregatedMeteringStats
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyApplication[F[_]: Monad](conseilConfig: ConseilConfig, apiKeyService: ApiKeyService[F]) {

  /** Returns all API keys from the DB */
  def getAllApiKeys(implicit session: Session): F[Permission[List[ApiKey]]] = requiredRole(Role.Administrator) {
    apiKeyService.getAllApiKeys
  }

  /** Checks if API key is valid */
  def validateApiKey(key: String): F[Boolean] =
    apiKeyService.validateApiKey(key)

  /** Refreshes api key */
  def refreshApiKey(environment: Environment)(implicit session: Session): F[ApiKey] =
    apiKeyService.refreshApiKey(session.userId, environment)

  /** Returns API Keys for current user with given ID */
  def getCurrentUserApiKeys(implicit session: Session): F[List[ApiKey]] =
    apiKeyService.getActiveApiKeys(session.userId)

  /** Returns API Keys usage for current user with given ID */
  def getCurrentApiKeysUsage(implicit session: Session): F[List[UsageLeft]] =
    apiKeyService.getApiKeysUsage(session.userId)

  /** Returns API Keys for user with given ID */
  def getApiKeys(userId: UserId)(implicit session: Session): F[Permission[List[ApiKey]]] =
    requiredRole(Administrator) {
      apiKeyService.getUserApiKeys(userId)
    }

  /** Returns all API keys from the DB */
  def getApiKeysForEnv(apiKey: String, environment: Environment): F[Permission[List[String]]] =
    requiredApiKey(apiKey, conseilConfig.keys)(apiKeyService.getAllApiKeysForEnv(environment))

  /** Returns API Keys usage for user with given ID */
  def getUserApiKeysUsage(userId: UserId)(implicit session: Session): F[Permission[List[UsageLeft]]] =
    requiredRole(Administrator) {
      apiKeyService.getApiKeysUsage(userId)
    }

  /** Returns API Keys query stats */
  def getMeteringStats(implicit session: Session): F[MeteringStats] =
    apiKeyService.getMeteringStats(session.userId)

  /** Returns API Keys query stats */
  def getAggregatedMeteringStats(userId: UserId, from: Option[Instant], apiKey: Option[String])(
      implicit session: Session
  ): F[Permission[List[AggregatedMeteringStats]]] =
    requiredRoleOrApiKey(Administrator, apiKey, conseilConfig.keys) {
      apiKeyService.getAggregatedMeteringStatsForUser(userId, from)
    }

  /** Deactivates API keys for query */
  def deactivateApiKeysForUsers(userIds: List[UserId], apiKey: String): F[Permission[Unit]] =
    requiredApiKey(apiKey, conseilConfig.keys) {
      apiKeyService.deactivateApiKeysForUsers(userIds)
    }

  /** Deactivates API keys for query */
  def activateApiKeysForUsers(userIds: List[UserId], apiKey: String): F[Permission[Unit]] =
    requiredApiKey(apiKey, conseilConfig.keys) {
      apiKeyService.activateApiKeysForUsers(userIds)
    }
}
