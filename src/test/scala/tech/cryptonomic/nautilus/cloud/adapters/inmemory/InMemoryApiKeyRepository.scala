package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import java.time.Instant

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.DoobieApiKeyRepository._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{
  ApiKey,
  ApiKeyRepository,
  CreateApiKey,
  Environment,
  RefreshApiKey,
  UsageLeft
}
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
  override def updateApiKey(refreshApiKey: RefreshApiKey): F[ApiKey] = this.synchronized {
    val apiKey = refreshApiKey.toCreateApiKey.toApiKey(apiKeys.map(_.keyId).maximumOption.getOrElse(0) + 1)

    apiKeys = apiKeys.collect {
        case apiKey: ApiKey
            if (apiKey.environment == refreshApiKey.environment && apiKey.userId == refreshApiKey.userId) =>
          apiKey.copy(dateSuspended = Some(refreshApiKey.now))
        case it => it
      } :+ apiKey

    apiKey.pure[F]
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

  /** Gets keys for environment */
  override def getKeysForEnv(environment: Environment): F[List[String]] =
    apiKeys.filter(_.environment == environment).map(_.key).pure[F]

  /** Invalidate all API keys connected to user */
  override def invalidateApiKeys(userId: UserId, now: Instant): F[Unit] = this.synchronized {
    apiKeys = apiKeys.filterNot(_.userId == userId)
    ().pure[F]
  }

  /** Gets keys which were active during last month */
  override def getUserActiveKeysForLastMonth(userId: UserId): F[List[ApiKey]] = this.synchronized {
    import scala.concurrent.duration._

    apiKeys
      .filter(
        key =>
          key.userId == userId && key.dateIssued.exists(
              _.isAfter(Instant.now().minusSeconds(30.days.toSeconds))
            )
      )
      .pure[F]
  }

  /** Gets keys which were active in between dates */
  override def getUserActiveKeysInGivenRange(userId: UserId, start: Instant, end: Instant): F[List[ApiKey]] =
    this.synchronized {
      apiKeys.filter { key =>
        key.userId == userId &&
        (key.dateIssued.exists(di => di.isAfter(start) && di.isBefore(end)) ||
        key.dateSuspended.exists(ds => ds.isAfter(start) && ds.isBefore(end)) ||
          (key.dateIssued.exists(di => di.isBefore(end))  && key.dateSuspended.isEmpty))
      }.pure[F]
    }
}
