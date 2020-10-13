package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.Bracket
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.adapters.doobie.DoobieApiKeyRepository.ExtendedRefreshApiKey
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** Trait representing API Key repo queries */
class DoobieApiKeyRepository[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable])
    extends ApiKeyRepository[F]
    with ApiKeyQueries {

  /** Query returning all API keys from the DB */
  override def getAllApiKeys: F[List[ApiKey]] =
    getAllApiKeysQuery.to[List].transact(transactor)

  /** Query checking if API key is valid */
  override def validateApiKey(apiKey: String): F[Boolean] =
    validateApiKeyQuery(apiKey).nel.map(_.head).transact(transactor)

  /** Query returning API keys connected to user */
  override def getUserApiKeys(userId: Int): F[List[ApiKey]] =
    getUserApiKeysQuery(userId).to[List].transact(transactor)

  /** Query returning API keys usage for given user */
  override def updateKeyUsage(usage: UsageLeft): F[Unit] =
    updateUsageQuery(usage).run.void.transact(transactor)

  /** Query updating API keys connected to user */
  override def updateApiKey(refreshApiKey: RefreshApiKey): F[ApiKey] =
    (for {
      _ <- invalidateApiKeyQuery(refreshApiKey.toInvalidateApiKey).run
      createApiKey = refreshApiKey.toCreateApiKey
      id <- putApiKeyQuery(createApiKey).run
    } yield createApiKey.toApiKey(id)).transact(transactor)

  /** Query returning API keys usage for given user */
  override def getKeysUsageForUser(userId: Int): F[List[UsageLeft]] =
    getUsageForUserQuery(userId).to[List].transact(transactor)

  /** Query returning API key usage */
  override def getKeyUsage(key: String): F[Option[UsageLeft]] =
    getUsageForKeyQuery(key).option.transact(transactor)

  /** Inserts API key usage */
  override def putApiKeyUsage(usageLeft: UsageLeft): F[Unit] =
    putUsageQuery(usageLeft).run.void.transact(transactor)

  /** Inserts API key */
  override def putApiKey(apiKey: CreateApiKey): F[Unit] =
    putApiKeyQuery(apiKey).run.void.transact(transactor)

  override def getCurrentActiveApiKeys(id: UserId): F[List[ApiKey]] =
    getActiveApiKeysQuery(id).to[List].transact(transactor)

  /** Gets keys for environment */
  override def getKeysForEnv(environment: Environment): F[List[String]] =
    getKeysForEnvQuery(environment).to[List].transact(transactor)

  /** Invalidate all API keys connected to user */
  override def deactivateApiKeysForUser(userId: UserId, now: Instant): F[Unit] =
    invalidateApiKeysQuery(userId, now).run.void.transact(transactor)

  /** Gets keys which were active during last month */
  override def getUserActiveKeysForLastMonth(userId: UserId): F[List[ApiKey]] =
    getUserKeysForLastMonthQuery(userId).to[List].transact(transactor)

  /** Gets keys which were active in between dates */
  override def getUserActiveKeysInGivenRange(userId: UserId, start: Instant, end: Instant): F[List[ApiKey]] =
    getUserKeysValidIn(userId, start, end).to[List].transact(transactor)

  /** Validate all API keys connected to user */
  override def activateApiKeysForUser(userId: UserId): F[Unit] =
    validateApiKeysQuery(userId).run.void.transact(transactor)

}

case class InvalidateApiKey(
    environment: Environment,
    userId: UserId,
    now: Instant
)

object DoobieApiKeyRepository {
  implicit class ExtendedRefreshApiKey(val refreshApiKey: RefreshApiKey) extends AnyVal {
    def toCreateApiKey =
      CreateApiKey(refreshApiKey.apiKey, refreshApiKey.environment, refreshApiKey.userId, refreshApiKey.now, None)
    def toInvalidateApiKey = InvalidateApiKey(refreshApiKey.environment, refreshApiKey.userId, refreshApiKey.now)
  }
}
