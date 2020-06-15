package tech.cryptonomic.nautilus.cloud.domain.apiKey
import java.time.Instant

/** Representation of stats per ApiKey */
case class ApiKeyStats(time: Instant, count: Int, apiKey: Option[String])

/** Representation of stats per Route */
case class RouteStats(time: Instant, count: Int, uri: String, apiKey: Option[String])

/** Representation of stats per IP */
case class IpStats(time: Instant, count: Int, ip: String, apiKey: Option[String])

/** Case class combining results of all stats */
case class MeteringStats(
    apiKeyStats5m: List[ApiKeyStats],
    apiKeyStats24h: List[ApiKeyStats],
    routeStats5m: List[RouteStats],
    routeStats24h: List[RouteStats],
    ipStats5m: List[IpStats],
    ipStats24h: List[IpStats]
)
