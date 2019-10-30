package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.domain.pagination.PaginatedResult
import tech.cryptonomic.nautilus.cloud.domain.user.{AdminUpdateUser, UpdateCurrentUser, User}

/** Schemas used for User endpoints */
trait UserSchemas extends algebra.JsonSchemas with ApiKeySchemas {

  /** User registration schema */
  implicit lazy val userRegSchema: JsonSchema[AdminUpdateUser] =
    genericJsonSchema[AdminUpdateUser]

  /** Current user registration schema */
  implicit lazy val currentUserRegSchema: JsonSchema[UpdateCurrentUser] =
    genericJsonSchema[UpdateCurrentUser]

  /** User schema */
  implicit lazy val userSchema: JsonSchema[User] =
    genericJsonSchema[User]

  /** Paginated users schema */
  implicit lazy val paginatedResultsSchema: JsonSchema[PaginatedResult[User]] =
    genericJsonSchema[PaginatedResult[User]]
}
