package tech.cryptonomic.nautilus.cloud.domain

import cats.Monad
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{requiredRole, Permission}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyApplication[F[_]: Monad](apiKeyService: ApiKeyService[F]) {

  /** Returns all API keys from the DB */
  def getAllApiKeys(implicit session: Session): F[Permission[List[ApiKey]]] = requiredRole(Role.Administrator) {
    apiKeyService.getAllApiKeys
  }

  /** Checks if API key is valid */
  def validateApiKey(key: String): F[Boolean] =
    apiKeyService.validateApiKey(key)

  /** Refreshes api key */
  def refreshApiKey(environment: Environment)(implicit session: Session): F[Unit] =
    apiKeyService.refreshApiKey(session.userId, environment)

  /** Returns API Keys for current user with given ID */
  def getCurrentUserApiKeys(implicit session: Session): F[List[ApiKey]] =
    apiKeyService.getActiveApiKeys(session.userId)

  /** Returns API Keys usage for current user with given ID */
  def getCurrentUserApiKeysUsage(implicit session: Session): F[List[UsageLeft]] =
    apiKeyService.getApiKeysUsage(session.userId)

  /** Returns API Keys for user with given ID */
  def getApiKeys(userId: UserId)(implicit session: Session): F[Permission[List[ApiKey]]] =
    requiredRole(Administrator) {
      apiKeyService.getUserApiKeys(userId)
    }

  /** Returns API Keys usage for user with given ID */
  def getUserApiKeysUsage(userId: UserId)(implicit session: Session): F[Permission[List[UsageLeft]]] =
    requiredRole(Administrator) {
      apiKeyService.getApiKeysUsage(userId)
    }
}
