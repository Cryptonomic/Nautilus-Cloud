package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.domain.user.history.UserAction

/** Schemas for user history endpoints */
trait UserHistorySchemas extends algebra.JsonSchemas with InstantSchema {

  /** User action schema */
  implicit lazy val userActionSchema: JsonSchema[UserAction] =
    genericJsonSchema[UserAction]
}
