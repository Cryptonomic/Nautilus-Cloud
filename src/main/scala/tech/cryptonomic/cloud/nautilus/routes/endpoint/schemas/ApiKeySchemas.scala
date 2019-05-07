package tech.cryptonomic.cloud.nautilus.routes.endpoint.schemas

import endpoints.generic
import tech.cryptonomic.cloud.nautilus.model.ApiKey

/** Schemas used for ApiKey endpoints */
trait ApiKeySchemas extends generic.JsonSchemas {

  /** ApiKey schema */
  implicit lazy val apiKeySchema: JsonSchema[ApiKey] =
    genericJsonSchema[ApiKey]

  /** Timestamp schema */
  implicit lazy val timestampSchema: JsonSchema[java.sql.Timestamp] =
    xmapJsonSchema[Long, java.sql.Timestamp](
      implicitly[JsonSchema[Long]],
      millisFromEpoch => new java.sql.Timestamp(millisFromEpoch),
      ts => ts.getTime
    )
}
