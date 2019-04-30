package tech.cryptonomic.cloud.nautilus.routes.endpoint.schemas

import endpoints.{algebra, generic}
import tech.cryptonomic.cloud.nautilus.model.{User, UserReg}

/** Schemas used for User endpoints */
trait UserSchemas extends algebra.JsonSchemas with generic.JsonSchemas with ApiKeySchemas {
  /** User registration schema */
  implicit lazy val userRegSchema: JsonSchema[UserReg] =
    genericJsonSchema[UserReg]

  /** User schema */
  implicit lazy val userSchema: JsonSchema[User] =
    genericJsonSchema[User]
}
