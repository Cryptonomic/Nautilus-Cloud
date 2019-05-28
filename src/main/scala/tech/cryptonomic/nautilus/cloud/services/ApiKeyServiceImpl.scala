package tech.cryptonomic.nautilus.cloud.services

import tech.cryptonomic.nautilus.cloud.repositories.ApiKeyRepo
import tech.cryptonomic.nautilus.cloud.model.ApiKey

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
