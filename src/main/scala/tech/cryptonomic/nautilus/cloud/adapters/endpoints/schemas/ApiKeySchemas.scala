package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.generic
import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.{ApiKey, Environment, UsageLeft}
import tech.cryptonomic.nautilus.cloud.application.domain.tier.Usage
import tech.cryptonomic.nautilus.cloud.application.domain.user.{AuthenticationProvider, Role}

/** Schemas used for ApiKey endpoints */
trait ApiKeySchemas extends generic.JsonSchemas with InstantSchema with EnvironmentSchema {

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
}
