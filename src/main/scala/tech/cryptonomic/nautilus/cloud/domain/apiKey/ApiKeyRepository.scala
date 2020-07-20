package tech.cryptonomic.nautilus.cloud.domain.apiKey

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** Trait representing API Key repo queries */
trait ApiKeyRepository[F[_]] {

  /** Query returning all API keys from the DB */
  def getAllApiKeys: F[List[ApiKey]]

  /** Query returning currently active API keys*/
  def getCurrentActiveApiKeys(id: UserId): F[List[ApiKey]]

  /** Query checking if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean]

  /** Query returning API keys connected to user */
  def getUserApiKeys(userId: Int): F[List[ApiKey]]

  /** Update API keys connected to user */
  def updateApiKey(refreshApiKey: RefreshApiKey): F[ApiKey]

  /** Invalidate all API keys connected to user */
  def invalidateApiKeys(userId: UserId, now: Instant): F[Unit]

  /** Inserts API key */
  def putApiKey(apiKey: CreateApiKey): F[Unit]

  /** Inserts API key usage */
  def putApiKeyUsage(usageLeft: UsageLeft): F[Unit]

  /** Query returning API keys usage for given user */
  def getKeysUsageForUser(userId: Int): F[List[UsageLeft]]

  /** Query returning API key usage */
  def getKeyUsage(key: String): F[Option[UsageLeft]]

  /** Updates API key usage */
  def updateKeyUsage(usage: UsageLeft): F[Unit]

  /** Gets keys for environment */
  def getKeysForEnv(environment: Environment): F[List[String]]

  /** Gets keys which were active during last month */
  def getUserActiveKeysForLastMonth(userId: Int): F[List[ApiKey]]

  /** Gets keys which were active in between dates */
  def getUserActiveKeysInGivenRange(userId: Int, start: Instant, end: Instant): F[List[ApiKey]]

}
