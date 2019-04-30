package tech.cryptonomic.cloud.nautilus.services

import tech.cryptonomic.cloud.nautilus.model.ApiKey

import scala.language.higherKinds

trait ApiKeyService[F[_]] {

  def getAllApiKeys: F[List[ApiKey]]

  def validateApiKey(apiKey: String): F[Boolean]
}
