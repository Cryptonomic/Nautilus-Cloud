package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.UserHistorySchemas
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.history.UserAction

trait UserHistoryEndpoints
    extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with UserHistorySchemas
    with EndpointsStatusDefinitions {

  /** Endpoint definition for validation of API Key */
  def getCurrentUserActionsEndpoint: Endpoint[Unit, List[UserAction]] =
    endpoint(
      request = get(url = path / "users" / "me" / "history"),
      response = jsonResponse[List[UserAction]](),
      tags = List("UserHistory")
    )

  /** User keys endpoint definition */
  def getUserActionsEndpoint: Endpoint[UserId, Permission[List[UserAction]]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("userId") / "history"),
      response = jsonResponse[List[UserAction]]().orForbidden(),
      tags = List("UserHistory")
    )
}
