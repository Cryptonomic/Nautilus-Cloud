package tech.cryptonomic.nautilus.cloud.domain.metering.api
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyStats, IpStats, RouteStats}
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApi.Result

import scala.language.higherKinds

/** Interface for metering API */
trait MeteringApi[F[_]] {
  /** Fetches ApiKey stats per 5m */
  def getApiKey5mStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]]

  /** Fetches ApiKey stats per 24h */
  def getApiKey24hStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]]

  /** Fetches Route stats per 5m */
  def getRoute5mStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]]

  /** Fetches Route stats per 24h */
  def getRoute24hStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]]

  /** Fetches IP stats per 5m */
  def getIp5mStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]]

  /** Fetches IP stats per 24h */
  def getIp24hStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]]
}

/** Companion object for Metering API */
object MeteringApi {
  /** Type representing result of fetching from Metering API*/
  type Result[T] = Either[Throwable, T]
}
