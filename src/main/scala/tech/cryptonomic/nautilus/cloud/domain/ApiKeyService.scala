package tech.cryptonomic.nautilus.cloud.domain

import cats.Applicative
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, requiredRole}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.Role

import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyService[F[_]: Applicative](apiKeysRepo: ApiKeyRepository[F]) {

  /** Returns all API keys from the DB */
  def getAllApiKeys(implicit session: Session): F[Permission[List[ApiKey]]] = requiredRole(Role.Administrator) {
    apiKeysRepo.getAllApiKeys
  }

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean] =
    apiKeysRepo.validateApiKey(apiKey)
}
