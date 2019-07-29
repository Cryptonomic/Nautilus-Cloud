package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.generic
import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.Environment

/* Schema for Environment  */
trait EnvironmentSchema extends generic.JsonSchemas {
  /** Environment */
  implicit lazy val environment: JsonSchema[Environment] =
    xmapJsonSchema[String, Environment](
      implicitly[JsonSchema[String]],
      envName => Environment.byName(envName),
      _.name
    )
}
