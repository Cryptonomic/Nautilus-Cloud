package tech.cryptonomic.cloud.nautilus.repositories

import doobie.Query0
import doobie.implicits._
import tech.cryptonomic.cloud.nautilus.model.ApiKey

class ApiKeyRepoImpl extends ApiKeyRepo {

  override def getAllApiKeys: Query0[ApiKey] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keys"
      .query[ApiKey]

  override def validateApiKey(apiKey: String): Query0[Boolean] =
    sql"SELECT exists (SELECT 1 FROM api_keys WHERE key = $apiKey LIMIT 1)"
      .query[Boolean]

  override def getUserApiKeys(userId: Int): Query0[ApiKey] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keys WHERE userid = $userId"
      .query[ApiKey]
}
