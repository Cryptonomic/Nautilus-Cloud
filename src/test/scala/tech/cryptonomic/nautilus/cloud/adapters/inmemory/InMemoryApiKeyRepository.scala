package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Monad
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyRepository
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

class InMemoryApiKeyRepository[F[_]](implicit monad: Monad[F]) extends ApiKeyRepository[F] {

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
    monad.pure(apiKeys)
  }

  /** Query checking if API key is valid */
  override def validateApiKey(apiKey: String): F[Boolean] = this.synchronized {
    monad.pure(apiKeys.exists(_.key == apiKey))
  }

  /** Query returning API keys connected to user */
  override def getUserApiKeys(userId: UserId): F[List[ApiKey]] = this.synchronized {
    monad.pure(apiKeys.filter(_.userId == userId))
  }

  /** Clears repository */
  def clear(): Unit = this.synchronized {
    apiKeys = List.empty
  }
}
