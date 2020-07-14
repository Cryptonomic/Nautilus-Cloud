package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, CreateApiKey, Environment, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/** Trait containing api key related queries */
trait ApiKeyQueries extends EnvironmentMappers {

  /** Query returning all API keys from the DB */
  def getAllApiKeysQuery: Query0[ApiKey] =
    sql"SELECT keyid, key, environment, userid, dateissued, datesuspended FROM api_keys"
      .query[ApiKey]

  /** Query returning all API keys from the DB */
  def getActiveApiKeysQuery(userId: UserId): Query0[ApiKey] =
    sql"SELECT keyid, key, environment, userid, dateissued, datesuspended FROM api_keys where userid = $userId AND datesuspended IS NULL"
      .query[ApiKey]

  /** Query checking if API key is valid */
  def validateApiKeyQuery(apiKey: String): Query0[Boolean] =
    sql"SELECT exists (SELECT 1 FROM api_keys WHERE key = $apiKey and datesuspended IS NULL LIMIT 1)"
      .query[Boolean]

  /** Query returning API keys connected to user */
  def getUserApiKeysQuery(userId: UserId): Query0[ApiKey] =
    sql"SELECT keyid, key, environment, userid, dateissued, datesuspended FROM api_keys WHERE userid = $userId"
      .query[ApiKey]

  /** Inserts API key for user */
  def putApiKeyQuery(apiKey: CreateApiKey): Update0 =
    sql"INSERT INTO api_keys (key, environment, userid, dateissued, datesuspended) VALUES(${apiKey.key}, ${apiKey.environment.name}, ${apiKey.userId}, ${apiKey.dateIssued}, ${apiKey.dateSuspended})".update

  /** Inserts API key for user */
  def invalidateApiKeyQuery(invalidateApiKey: InvalidateApiKey): Update0 =
    sql"UPDATE api_keys SET datesuspended = ${invalidateApiKey.now} WHERE environment = ${invalidateApiKey.environment} AND userid = ${invalidateApiKey.userId}".update

  /** Inserts API key for user */
  def invalidateApiKeysQuery(userId: UserId, now: Instant): Update0 =
    sql"UPDATE api_keys SET datesuspended = $now WHERE userid = $userId".update

  /** Query returning API keys usage for given user */
  def getUsageForUserQuery(userId: UserId): Query0[UsageLeft] =
    sql"SELECT key, monthly, daily FROM api_keys JOIN usage_left USING(key) WHERE userid = $userId"
      .query[UsageLeft]

  /** Query returning usage for given key */
  def getUsageForKeyQuery(key: String): Query0[UsageLeft] =
    sql"SELECT key, monthly, daily FROM usage_left WHERE key = $key"
      .query[UsageLeft]

  /** Query updates API keys usage */
  def updateUsageQuery(usage: UsageLeft): Update0 =
    sql"UPDATE usage_left SET daily = ${usage.daily}, monthly = ${usage.monthly} FROM api_keys a WHERE a.key = ${usage.key}".update

  /** Query updates API keys usage */
  def putUsageQuery(usage: UsageLeft): Update0 =
    sql"INSERT INTO usage_left(key, daily, monthly) VALUES(${usage.key}, ${usage.daily}, ${usage.monthly})".update

  /** Query selecting API keys for given env */
  def getKeysForEnvQuery(environment: Environment): Query0[String] =
    sql"SELECT key FROM api_keys WHERE environment = ${environment.name} AND datesuspended IS NULL".query[String]

  /** Query returning API keys connected to user */
  def getUserKeysForLastMonthQuery(userId: UserId): Query0[ApiKey] =
    sql"SELECT keyid, key, environment, userid, dateissued, datesuspended FROM api_keys WHERE userid = $userId AND dateissued > current_date - interval '30 days'"
      .query[ApiKey]

  /** Query returning API keys connected to user active in the given range of time */
  def getUserKeysValidIn(userId: UserId, begin: Instant, end: Instant): Query0[ApiKey] =
    sql"SELECT keyid, key, environment, userid, dateissued, datesuspended FROM api_keys WHERE userid = $userId AND ((dateissued >= $begin AND dateissued <= $end) OR (datesuspended >= $begin AND datesuspended <= $end) OR (dateissued <= $end AND datesuspended IS NULL))"
      .query[ApiKey]

}
