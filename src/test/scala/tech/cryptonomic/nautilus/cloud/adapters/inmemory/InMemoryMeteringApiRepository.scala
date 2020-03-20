package tech.cryptonomic.nautilus.cloud.adapters.inmemory
import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyStats, IpStats, RouteStats}
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApi
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApi.Result

import scala.language.higherKinds

class InMemoryMeteringApiRepository[F[_]: Monad] extends MeteringApi[F] {

  private var apiKeyStats5m: List[ApiKeyStats] = List.empty
  private var routeStats5m: List[RouteStats] = List.empty
  private var ipStats5m: List[IpStats] = List.empty

  private var apiKeyStats24h: List[ApiKeyStats] = List.empty
  private var routeStats24h: List[RouteStats] = List.empty
  private var ipStats24h: List[IpStats] = List.empty

  def addApiKeyStats5m(list: List[ApiKeyStats]): Unit = this.synchronized(apiKeyStats5m ++= list)

  def addRouteStats5m(list: List[RouteStats]): Unit = this.synchronized(routeStats5m ++= list)

  def addIpStats5m(list: List[IpStats]): Unit = this.synchronized(ipStats5m ++= list)

  def addApiKeyStats24h(list: List[ApiKeyStats]): Unit = this.synchronized(apiKeyStats24h ++= list)

  def addRouteStats24h(list: List[RouteStats]): Unit = this.synchronized(routeStats24h ++= list)

  def addIpStats24h(list: List[IpStats]): Unit = this.synchronized(ipStats24h ++= list)

  override def getApiKey5mStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]] = this.synchronized {
    Either
      .cond(true, apiKeyStats5m.filter(stats => apiKeys.map(_.key).contains(stats.apiKey.getOrElse(""))), new Throwable)
      .pure[F]
  }
  override def getApiKey24hStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]] = this.synchronized {
    Either
      .cond(
        true,
        apiKeyStats24h.filter(stats => apiKeys.map(_.key).contains(stats.apiKey.getOrElse(""))),
        new Throwable
      )
      .pure[F]
  }
  override def getRoute5mStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]] = this.synchronized {
    Either
      .cond(true, routeStats5m.filter(stats => apiKeys.map(_.key).contains(stats.apiKey.getOrElse(""))), new Throwable)
      .pure[F]
  }
  override def getRoute24hStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]] = this.synchronized {
    Either
      .cond(true, routeStats24h.filter(stats => apiKeys.map(_.key).contains(stats.apiKey.getOrElse(""))), new Throwable)
      .pure[F]
  }
  override def getIp5mStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]] = this.synchronized {
    Either
      .cond(true, ipStats5m.filter(stats => apiKeys.map(_.key).contains(stats.apiKey.getOrElse(""))), new Throwable)
      .pure[F]
  }
  override def getIp24hStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]] = this.synchronized {
    Either
      .cond(true, ipStats24h.filter(stats => apiKeys.map(_.key).contains(stats.apiKey.getOrElse(""))), new Throwable)
      .pure[F]
  }
}
