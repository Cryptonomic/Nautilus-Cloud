package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.generic
import tech.cryptonomic.nautilus.cloud.application.domain.resources.{CreateResource, Resource}

/** JSON schemas for resource related models */
trait ResourceSchemas extends generic.JsonSchemas with EnvironmentSchema {

  /** Resource JSON schema*/
  implicit lazy val resourceSchema: JsonSchema[Resource] =
    genericJsonSchema[Resource]

  /** JSON schema for creating resource */
  implicit lazy val createResourceSchema: JsonSchema[CreateResource] =
    genericJsonSchema[CreateResource]
}
