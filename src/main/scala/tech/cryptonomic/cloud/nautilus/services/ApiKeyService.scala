package tech.cryptonomic.cloud.nautilus.services

import cats.effect.IO
import tech.cryptonomic.cloud.nautilus.model.ApiKey
import tech.cryptonomic.cloud.nautilus.repositories.ApiKeyRepo

import scala.concurrent.Future

trait ApiKeyService {

  def getAllApiKeys: Future[List[ApiKey]]

  def validateApiKey(apiKey: String): Future[Boolean]
}

class ApiKeyServiceImpl(apiKeysRepo: ApiKeyRepo[IO]) extends ApiKeyService {
  override def getAllApiKeys: Future[List[ApiKey]] =
    apiKeysRepo.getAllApiKeys.unsafeToFuture()

  override def validateApiKey(apiKey: String): Future[Boolean] =
    apiKeysRepo.validateApiKey(apiKey).unsafeToFuture()
}
