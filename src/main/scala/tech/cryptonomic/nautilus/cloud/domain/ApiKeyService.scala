package tech.cryptonomic.nautilus.cloud.domain

import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository}

import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyService[F[_]](apiKeysRepo: ApiKeyRepository[F]) {

  /** Returns all API keys from the DB */
  def getAllApiKeys: F[List[ApiKey]] =
    apiKeysRepo.getAllApiKeys

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean] =
    apiKeysRepo.validateApiKey(apiKey)
}
