package tech.cryptonomic.cloud.nautilus.repositories.dao

import doobie.util.query.Query0
import tech.cryptonomic.cloud.nautilus.model.ApiKey
import doobie.implicits._

/** Trait containing API Key related queries */
trait ApiKeyDao {

  /** Query returning all API keys from the DB */
  def getAllApiKeysQuery: Query0[ApiKey] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keys"
      .query[ApiKey]

  /** Query checking if API key is valid */
  def validateApiKeyQuery(apiKey: String): Query0[Boolean] =
    sql"SELECT exists (SELECT 1 FROM api_keys WHERE key = $apiKey LIMIT 1)"
      .query[Boolean]

  /** Query returning API keys connected to user */
  def getUserApiKeysQuery(userId: Int): Query0[ApiKey] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keys WHERE userid = $userId"
      .query[ApiKey]
}
