package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.generic
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, Role}

/** Schemas used for ApiKey endpoints */
trait ApiKeySchemas extends generic.JsonSchemas with InstantSchema {

  /** ApiKey schema */
  implicit lazy val apiKeySchema: JsonSchema[ApiKey] =
    genericJsonSchema[ApiKey]

  /** Authentication provider */
  implicit lazy val authenticationProviderSchema: JsonSchema[AuthenticationProvider] =
    xmapJsonSchema[String, AuthenticationProvider](
      implicitly[JsonSchema[String]],
      providerName => AuthenticationProvider.byName(providerName),
      _.name
    )

  /** Authentication provider */
  implicit lazy val roleSchema: JsonSchema[Role] =
    xmapJsonSchema[String, Role](
      implicitly[JsonSchema[String]],
      roleName => Role.byName(roleName),
      _.name
    )
}
