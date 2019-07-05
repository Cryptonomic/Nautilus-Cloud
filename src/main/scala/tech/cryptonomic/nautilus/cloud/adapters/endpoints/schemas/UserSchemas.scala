package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{CreateApiKeyRequest, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User}

/** Schemas used for User endpoints */
trait UserSchemas extends algebra.JsonSchemas with ApiKeySchemas {

  /** Create user schema */
  implicit lazy val createUserSchema: JsonSchema[CreateUser] =
    genericJsonSchema[CreateUser]

  /** User registration schema */
  implicit lazy val userRegSchema: JsonSchema[UpdateUser] =
    genericJsonSchema[UpdateUser]

  /** User schema */
  implicit lazy val userSchema: JsonSchema[User] =
    genericJsonSchema[User]

  /** Usage schema */
  implicit lazy val usageSchema: JsonSchema[UsageLeft] =
    genericJsonSchema[UsageLeft]

  /** API key creation schema */
  implicit lazy val apiKeyCreationRequestSchema: JsonSchema[CreateApiKeyRequest] =
    genericJsonSchema[CreateApiKeyRequest]

}
