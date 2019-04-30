package tech.cryptonomic.cloud.nautilus.services

import cats.effect.Bracket
import doobie.util.transactor.Transactor
import tech.cryptonomic.cloud.nautilus.model.ApiKey
import tech.cryptonomic.cloud.nautilus.repositories.ApiKeyRepo
import doobie.implicits._
import scala.language.higherKinds

class ApiKeyServiceImpl[F[_]](apiKeysRepo: ApiKeyRepo, transactor: Transactor[F])(
    implicit bracket: Bracket[F, Throwable]
) extends ApiKeyService[F] {
  override def getAllApiKeys: F[List[ApiKey]] =
    apiKeysRepo.getAllApiKeys.to[List].transact(transactor)

  override def validateApiKey(apiKey: String): F[Boolean] =
    apiKeysRepo.validateApiKey(apiKey).nel.map(_.head).transact(transactor)
}
