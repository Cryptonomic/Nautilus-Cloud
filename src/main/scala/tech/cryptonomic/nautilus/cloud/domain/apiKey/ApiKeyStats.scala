package tech.cryptonomic.nautilus.cloud.domain.apiKey
import java.time.Instant

case class ApiKeyStats(time: Instant, count: Int, apiKey: Option[String])

case class RouteStats(time: Instant, count: Int, uri: String, apiKey: Option[String])

case class IpStats(time: Instant, count: Int, ip: String, apiKey: Option[String])

case class MeteringStats(
    apiKeyStats5m: List[ApiKeyStats],
    apiKeyStats24h: List[ApiKeyStats],
    routeStats5m: List[RouteStats],
    routeStats24h: List[RouteStats],
    ipStats5m: List[IpStats],
    ipStats24h: List[IpStats]
)
