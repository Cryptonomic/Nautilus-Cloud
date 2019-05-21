package tech.cryptonomic.cloud.nautilus.adapters.endpoints.schemas

import endpoints.algebra
import tech.cryptonomic.cloud.nautilus.adapters.endpoints.UsageLeft
import tech.cryptonomic.cloud.nautilus.domain.user.{User, UserWithoutId}

/** Schemas used for User endpoints */
trait UserSchemas extends algebra.JsonSchemas with ApiKeySchemas {

  /** User registration schema */
  implicit lazy val userRegSchema: JsonSchema[UserWithoutId] =
    genericJsonSchema[UserWithoutId]

  /** User schema */
  implicit lazy val userSchema: JsonSchema[User] =
    genericJsonSchema[User]

  /** Usage schema */
  implicit lazy val usageSchema: JsonSchema[UsageLeft] =
    genericJsonSchema[UsageLeft]
}
