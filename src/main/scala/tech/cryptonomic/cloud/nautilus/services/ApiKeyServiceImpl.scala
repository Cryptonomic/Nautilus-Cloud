package tech.cryptonomic.cloud.nautilus.services

import tech.cryptonomic.cloud.nautilus.model.ApiKey
import tech.cryptonomic.cloud.nautilus.repositories.ApiKeyRepo
import scala.language.higherKinds

/** API keys service implementation */
class ApiKeyServiceImpl[F[_]](apiKeysRepo: ApiKeyRepo[F]) extends ApiKeyService[F] {

  /** Returns all API keys from the DB */
  override def getAllApiKeys: F[List[ApiKey]] =
    apiKeysRepo.getAllApiKeys

  /** Checks if API key is valid */
  override def validateApiKey(apiKey: String): F[Boolean] =
    apiKeysRepo.validateApiKey(apiKey)
}
