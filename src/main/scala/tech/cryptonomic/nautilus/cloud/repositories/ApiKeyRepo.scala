package tech.cryptonomic.nautilus.cloud.repositories

import tech.cryptonomic.nautilus.cloud.model.ApiKey

import scala.language.higherKinds

/** Trait representing API Key repo queries */
trait ApiKeyRepo[F[_]] {

  /** Query returning all API keys from the DB */
  def getAllApiKeys: F[List[ApiKey]]

  /** Query checking if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean]

  /** Query returning API keys connected to user */
  def getUserApiKeys(userId: Int): F[List[ApiKey]]
}
