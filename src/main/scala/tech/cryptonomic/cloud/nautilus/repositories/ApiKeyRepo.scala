package tech.cryptonomic.cloud.nautilus.repositories

import cats.effect.Bracket
import doobie._
import doobie.implicits._
import tech.cryptonomic.cloud.nautilus.model.ApiKey

import scala.language.higherKinds

trait ApiKeyRepo[F[_]] {
  def getAllApiKeys: F[List[ApiKey]]

  def validateApiKey(apiKey: String): F[Boolean]

  def getUserApiKeys(userId: Long): F[List[ApiKey]]
}

class ApiKeyRepoImpl[F[_]](transactor: Transactor[F])(implicit br: Bracket[F, Throwable]) extends ApiKeyRepo[F] {
  override def getAllApiKeys: F[List[ApiKey]] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keys"
      .query[ApiKey]
      .to[List]
      .transact(transactor)

  override def validateApiKey(apiKey: String): F[Boolean] =
    sql"SELECT exists (SELECT 1 FROM api_keys WHERE key = $apiKey LIMIT 1)"
      .query[Boolean]
      .nel
      .map(_.head)
      .transact(transactor)

  override def getUserApiKeys(userId: Long): F[List[ApiKey]] =
    sql"SELECT keyid, key, resourceid, userid, tierid, dateissued, datesuspended FROM api_keyd WHERE userid = $userId"
      .query[ApiKey]
      .to[List]
      .transact(transactor)
}
