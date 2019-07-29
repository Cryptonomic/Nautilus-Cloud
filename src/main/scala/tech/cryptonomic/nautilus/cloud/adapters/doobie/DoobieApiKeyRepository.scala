package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.{
  ApiKey,
  ApiKeyRepository,
  CreateApiKey,
  Environment,
  RefreshApiKey,
  UsageLeft
}
import tech.cryptonomic.nautilus.cloud.application.domain.user.User.UserId
import DoobieApiKeyRepository.ExtendedRefreshApiKey

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
    updateUsageQuery(usage).run.map(_ => ()).transact(transactor)

  /** Query updating API keys connected to user */
  override def updateApiKey(refreshApiKey: RefreshApiKey): F[Unit] = (for {
      _ <- invalidateApiKeyQuery(refreshApiKey.toInvalidateApiKey).run
      _ <- putApiKeyQuery(refreshApiKey.toCreateApiKey).run
    } yield ()).transact(transactor)

  /** Query returning API keys usage for given user */
  override def getKeysUsageForUser(userId: Int): F[List[UsageLeft]] =
    getUsageForUserQuery(userId).to[List].transact(transactor)

  /** Query returning API key usage */
  override def getKeyUsage(key: String): F[Option[UsageLeft]] =
    getUsageForKeyQuery(key).option.transact(transactor)

  /** Inserts API key usage */
  override def putApiKeyUsage(usageLeft: UsageLeft): F[Unit] =
    putUsageQuery(usageLeft).run.map(_ => ()).transact(transactor)

  /** Inserts API key */
  override def putApiKey(apiKey: CreateApiKey): F[Unit] =
    putApiKeyQuery(apiKey).run.map(_ => ()).transact(transactor)

  override def getCurrentActiveApiKeys(id: UserId): F[List[ApiKey]] =
    getActiveApiKeysQuery(id).to[List].transact(transactor)
}

object DoobieApiKeyRepository {
  implicit class ExtendedRefreshApiKey(val refreshApiKey: RefreshApiKey) extends AnyVal {
    def toCreateApiKey =
      CreateApiKey(refreshApiKey.apiKey, refreshApiKey.environment, refreshApiKey.userId, refreshApiKey.now, None)
    def toInvalidateApiKey = InvalidateApiKey(refreshApiKey.environment, refreshApiKey.userId, refreshApiKey.now)
  }
}

case class InvalidateApiKey(
    environment: Environment,
    userId: UserId,
    now: Instant
)
