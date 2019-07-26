package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.domain.apiKey.UsageLeft
import tech.cryptonomic.nautilus.cloud.domain.tier.Usage
import tech.cryptonomic.nautilus.cloud.domain.user.{UpdateUser, User}

/** Schemas used for User endpoints */
trait UserSchemas extends algebra.JsonSchemas with ApiKeySchemas {

  /** User registration schema */
  implicit lazy val userRegSchema: JsonSchema[UpdateUser] =
    genericJsonSchema[UpdateUser]

  /** User schema */
  implicit lazy val userSchema: JsonSchema[User] =
    genericJsonSchema[User]

  /** Usage left schema */
  implicit lazy val usageLeftSchema: JsonSchema[UsageLeft] =
    genericJsonSchema[UsageLeft]

  /** Usage schema */
  implicit lazy val usageSchema: JsonSchema[Usage] =
    genericJsonSchema[Usage]
}
