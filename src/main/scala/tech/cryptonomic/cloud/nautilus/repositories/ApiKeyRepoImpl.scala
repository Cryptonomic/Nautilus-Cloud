package tech.cryptonomic.cloud.nautilus.repositories

import doobie.Query0
import doobie.implicits._
import tech.cryptonomic.cloud.nautilus.model.ApiKey

/** Trait representing Doobie API Key repo queries */
class ApiKeyRepoImpl extends ApiKeyRepo {
  /** Query returning all API keys from the DB */
  override def getAllApiKeys: Query0[ApiKey] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keys"
      .query[ApiKey]

  /** Query checking if API key is valid */
  override def validateApiKey(apiKey: String): Query0[Boolean] =
    sql"SELECT exists (SELECT 1 FROM api_keys WHERE key = $apiKey LIMIT 1)"
      .query[Boolean]

  /** Query returning API keys connected to user */
  override def getUserApiKeys(userId: Int): Query0[ApiKey] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keys WHERE userid = $userId"
      .query[ApiKey]
}
