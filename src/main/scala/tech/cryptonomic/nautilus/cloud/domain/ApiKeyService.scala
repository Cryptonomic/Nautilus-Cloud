package tech.cryptonomic.nautilus.cloud.domain

import cats.Applicative
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.adapters.conseil.ConseilConfig
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{requiredRole, Permission}
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AccessDenied, Session}
import tech.cryptonomic.nautilus.cloud.domain.user.Role

import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyService[F[_]: Applicative](apiKeysRepo: ApiKeyRepository[F], conseilConfig: ConseilConfig) {

  /** Returns all API keys from the DB */
  def getAllApiKeys(implicit session: Session): F[Permission[List[ApiKey]]] = requiredRole(Role.Administrator) {
    apiKeysRepo.getAllApiKeys
  }

  /** Returns all API keys from the DB */
  def getAllApiKeysForEnv(apiKey: String, env: String): F[Permission[List[String]]] =
    Either
      .cond(
        conseilConfig.key == apiKey,
        apiKeysRepo.getKeysForEnv(env),
        AccessDenied("Wrong API key").pure[F]
      )
      .bisequence

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean] =
    apiKeysRepo.validateApiKey(apiKey)
}
