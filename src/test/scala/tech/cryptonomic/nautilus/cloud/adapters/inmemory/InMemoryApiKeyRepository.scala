package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.DoobieApiKeyRepository._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, CreateApiKey, RefreshApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

class InMemoryApiKeyRepository[F[_]: Monad] extends ApiKeyRepository[F] {

  /** list of all api keys
    *
    * in order to be consistent with a real database we adjust reads and writes to keep indexing starting from 1 not
    * from 0
    */
  private var apiKeys: List[ApiKey] = List.empty

  def add(apiKey: ApiKey): Unit = this.synchronized {
    apiKeys = apiKeys :+ apiKey
  }

  /** Query returning all API keys from the DB */
  override def getAllApiKeys: F[List[ApiKey]] = this.synchronized {
    apiKeys.pure[F]
  }

  /** Query checking if API key is valid */
  override def validateApiKey(apiKey: String): F[Boolean] = this.synchronized {
    apiKeys.exists(it => it.key == apiKey && it.dateSuspended.isEmpty).pure[F]
  }

  /** Query returning API keys connected to user */
  override def getUserApiKeys(userId: UserId): F[List[ApiKey]] = this.synchronized {
    apiKeys.filter(_.userId == userId).pure[F]
  }

  /** Clears repository */
  def clear(): Unit = this.synchronized {
    apiKeys = List.empty
  }

  /** Inserts API key */
  override def putApiKey(apiKey: CreateApiKey): F[Unit] = this.synchronized {
    (apiKeys = apiKeys :+ apiKey.toApiKey(apiKeys.map(_.keyId).maximumOption.getOrElse(0) + 1)).pure[F]
  }

  /** Query updating API keys connected to user */
  override def updateApiKey(refreshApiKey: RefreshApiKey): F[Unit] = this.synchronized {
    apiKeys = apiKeys.collect {
        case apiKey: ApiKey
            if (apiKey.environment == refreshApiKey.environment && apiKey.userId == refreshApiKey.userId) =>
          apiKey.copy(dateSuspended = Some(refreshApiKey.now))
        case it => it
      } :+ refreshApiKey.toCreateApiKey.toApiKey(apiKeys.map(_.keyId).maximumOption.getOrElse(0) + 1)

    ().pure[F]
  }

  private var apiKeyUsage: List[UsageLeft] = List.empty

  /** Inserts API key usage */
  override def putApiKeyUsage(usageLeft: UsageLeft): F[Unit] = this.synchronized {
    (apiKeyUsage = apiKeyUsage :+ usageLeft).pure[F]
  }

  /** Query returning API keys usage for given user */
  override def getKeysUsageForUser(userId: UserId): F[List[UsageLeft]] =
    apiKeys.filter(_.userId == userId).flatMap(ak => apiKeyUsage.filter(_.key == ak.key)).pure[F]

  /** Query returning API key usage */
  override def getKeyUsage(key: String): F[Option[UsageLeft]] =
    apiKeyUsage.find(_.key == key).pure[F]

  /** Updates API key usage */
  override def updateKeyUsage(usage: UsageLeft): F[Unit] =
    (apiKeyUsage = usage :: apiKeyUsage.filterNot(_.key == usage.key)).pure[F]

  override def getCurrentActiveApiKeys(id: UserId): F[List[ApiKey]] =
    apiKeys.filter(_.userId == id).filter(_.dateSuspended.isEmpty).pure[F]
}
