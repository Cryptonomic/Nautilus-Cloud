package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, CreateApiKey, UsageLeft}

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

}
