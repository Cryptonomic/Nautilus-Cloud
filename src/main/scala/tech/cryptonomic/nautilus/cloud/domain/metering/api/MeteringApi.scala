package tech.cryptonomic.nautilus.cloud.domain.metering.api
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyStats, IpStats, RouteStats}
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApi.Result

import scala.language.higherKinds

trait MeteringApi[F[_]] {
  def getApiKeyQueries5mStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]]
  def getApiKeyQueries24hStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]]
  def getRoute5mStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]]
  def getRoute24hStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]]
  def getIp5mStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]]
  def getIp24hStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]]
}

object MeteringApi {
  type Result[T] = Either[Throwable, T]
}
