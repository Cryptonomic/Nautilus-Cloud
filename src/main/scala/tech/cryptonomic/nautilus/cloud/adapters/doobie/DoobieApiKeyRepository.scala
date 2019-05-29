package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository}

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
}
