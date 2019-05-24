package tech.cryptonomic.nautilus.cloud.services

import tech.cryptonomic.nautilus.cloud.model.ApiKey

import scala.language.higherKinds

/** API keys service */
trait ApiKeyService[F[_]] {

  /** Returns all API keys from the DB */
  def getAllApiKeys: F[List[ApiKey]]

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean]
}
