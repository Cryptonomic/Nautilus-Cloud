package tech.cryptonomic.cloud.nautilus.adapters.endpoints.schemas

import java.time.{Instant, ZonedDateTime}

import endpoints.generic
import tech.cryptonomic.cloud.nautilus.domain.apiKey.ApiKey
import tech.cryptonomic.cloud.nautilus.domain.user.{AuthenticationProvider, Role}

/** Schemas used for ApiKey endpoints */
trait ApiKeySchemas extends generic.JsonSchemas {

  /** ApiKey schema */
  implicit lazy val apiKeySchema: JsonSchema[ApiKey] =
    genericJsonSchema[ApiKey]

  /** Timestamp schema */
  implicit lazy val timestampSchema: JsonSchema[Instant] =
    xmapJsonSchema[String, Instant](
      implicitly[JsonSchema[String]],
      it => ZonedDateTime.parse(it).toInstant,
      _.toString
    )

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
