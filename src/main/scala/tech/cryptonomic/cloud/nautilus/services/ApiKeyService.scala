package tech.cryptonomic.cloud.nautilus.services

import tech.cryptonomic.cloud.nautilus.model.ApiKey

import scala.language.higherKinds

/** API keys service */
trait ApiKeyService[F[_]] {

  /** Returns all API keys from the DB */
  def getAllApiKeys: F[List[ApiKey]]

  /** Checks if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean]
}
