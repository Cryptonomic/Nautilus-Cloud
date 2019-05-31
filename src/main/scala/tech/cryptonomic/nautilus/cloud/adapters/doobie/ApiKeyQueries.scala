package tech.cryptonomic.nautilus.cloud.adapters.doobie

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.UsageLeft
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey

/** Trait containing api key related queries */
trait ApiKeyQueries {

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

  /** Query returning API keys usage for given user */
  def getUsageForUser(userId: Int): Query0[UsageLeft] =
    sql"SELECT key, monthly, daily FROM api_keys JOIN usage_left USING(keyid) WHERE userid = $userId"
      .query[UsageLeft]

  /** Query returning usage for given key */
  def getUsageForKey(key: String): Query0[UsageLeft] =
    sql"SELECT key, monthly, daily FROM api_keys JOIN usage_left USING(keyid) WHERE key = $key"
      .query[UsageLeft]

  /** Query updates API keys usage */
  def updateUsage(usage: UsageLeft): Update0 =
    sql"UPDATE usage_left SET daily = ${usage.daily}, monthly = ${usage.monthly} FROM api_keys a WHERE a.key = ${usage.key} AND usage_left.keyid = a.keyid".update

}
