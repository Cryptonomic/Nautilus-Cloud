package tech.cryptonomic.nautilus.cloud.domain.apiKey

import tech.cryptonomic.nautilus.cloud.adapters.endpoints.UsageLeft

import scala.language.higherKinds

/** Trait representing API Key repo queries */
trait ApiKeyRepository[F[_]] {

  /** Query returning all API keys from the DB */
  def getAllApiKeys: F[List[ApiKey]]

  /** Query checking if API key is valid */
  def validateApiKey(apiKey: String): F[Boolean]

  /** Query returning API keys connected to user */
  def getUserApiKeys(userId: Int): F[List[ApiKey]]

  /** Query returning API keys usage for given user */
  def getKeysUsageForUser(userId: Int): F[List[UsageLeft]]

  /** Query returning API key usage */
  def getKeyUsage(key: String): F[Option[UsageLeft]]

  /** Updates API key usage */
  def updateKeyUsage(usage: UsageLeft): F[Unit]
}
