package tech.cryptonomic.cloud.nautilus.routes.endpoint.schemas

import endpoints.{algebra, generic}
import tech.cryptonomic.cloud.nautilus.model.{User, UserReg}

trait UserSchemas extends algebra.JsonSchemas with generic.JsonSchemas with ApiKeySchemas {
  implicit lazy val userRegSchema: JsonSchema[UserReg] =
    genericJsonSchema[UserReg]

  implicit lazy val userSchema: JsonSchema[User] =
    genericJsonSchema[User]
}
