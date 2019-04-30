package tech.cryptonomic.cloud.nautilus.repositories

import doobie._
import tech.cryptonomic.cloud.nautilus.model.ApiKey

trait ApiKeyRepo {
  def getAllApiKeys: Query0[ApiKey]

  def validateApiKey(apiKey: String): Query0[Boolean]

  def getUserApiKeys(userId: Int): Query0[ApiKey]
}
