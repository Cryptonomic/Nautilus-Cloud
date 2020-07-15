package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.AggregatedMeteringStats
import tech.cryptonomic.nautilus.cloud.domain.tier.Usage
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, Role}

/** Schemas used for ApiKey endpoints */
trait ApiKeySchemas extends InstantSchema with EnvironmentSchema {

  /** ApiKey schema */
  implicit lazy val apiKeySchema: JsonSchema[ApiKey] =
    genericJsonSchema[ApiKey]

  /** Usage left schema */
  implicit lazy val usageLeftSchema: JsonSchema[UsageLeft] =
    genericJsonSchema[UsageLeft]

  /** Usage schema */
  implicit lazy val usageSchema: JsonSchema[Usage] =
    genericJsonSchema[Usage]

  /** Authentication provider */
  implicit lazy val authenticationProviderSchema: JsonSchema[AuthenticationProvider] =
    xmapJsonSchema[String, AuthenticationProvider](
      implicitly[JsonSchema[String]],
      providerName => AuthenticationProvider.byName(providerName),
      _.name
    )

  /** Role schema */
  implicit lazy val roleSchema: JsonSchema[Role] =
    xmapJsonSchema[String, Role](
      implicitly[JsonSchema[String]],
      roleName => Role.byName(roleName),
      _.name
    )

  /** Apik Key stats schema */
  implicit lazy val apiKeyStatsSchema: JsonSchema[ApiKeyStats] =
    genericJsonSchema[ApiKeyStats]

  /** Route stats schema */
  implicit lazy val routeStatsSchema: JsonSchema[RouteStats] =
    genericJsonSchema[RouteStats]

  /** Ip stats schema */
  implicit lazy val ipStatsSchema: JsonSchema[IpStats] =
    genericJsonSchema[IpStats]

  /** Metering stats schema */
  implicit lazy val meteringStatsSchema: JsonSchema[MeteringStats] =
    genericJsonSchema[MeteringStats]

  /** Aggregated metering stats schema */
  implicit lazy val aggregatedMeteringStatsSchema: JsonSchema[AggregatedMeteringStats] =
    genericJsonSchema[AggregatedMeteringStats]
}
