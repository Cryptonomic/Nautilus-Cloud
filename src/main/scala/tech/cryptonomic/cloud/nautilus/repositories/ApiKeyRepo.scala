package tech.cryptonomic.cloud.nautilus.repositories

import doobie._
import tech.cryptonomic.cloud.nautilus.model.ApiKey

/** Trait representing Doobie API Key repo queries */
trait ApiKeyRepo {
  /** Query returning all API keys from the DB */
  def getAllApiKeys: Query0[ApiKey]

  /** Query checking if API key is valid */
  def validateApiKey(apiKey: String): Query0[Boolean]

  /** Query returning API keys connected to user */
  def getUserApiKeys(userId: Int): Query0[ApiKey]
}
