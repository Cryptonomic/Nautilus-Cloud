package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.UserSchemas
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.pagination.PaginatedResult
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{AdminUpdateUser, UpdateCurrentUser, UpdateUser, User}

/** User relevant endpoints */
trait UserEndpoints
    extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with UserSchemas
    with EndpointsStatusDefinitions {

  /** User update endpoint definition */
  def updateUser: Endpoint[(UserId, AdminUpdateUser), Permission[Unit]] =
    endpoint(
      request = put(url = path / "users" / segment[UserId]("userId"), jsonRequest[AdminUpdateUser]()),
      response = emptyResponse().withCreatedStatus().orForbidden(),
      tags = List("User")
    )

  /** Current user update endpoint definition */
  def updateCurrentUser: Endpoint[(UpdateCurrentUser), Unit] =
    endpoint(
      request = put(url = path / "users" / "me", jsonRequest[UpdateCurrentUser]()),
      response = emptyResponse().withCreatedStatus(),
      tags = List("User")
    )

  /** Currently logged-in user endpoint definition */
  def getCurrentUser: Endpoint[Unit, Option[User]] =
    endpoint(
      request = get(url = path / "users" / "me"),
      response = jsonResponse[User]().orNotFound(),
      tags = List("User")
    )

  /** Users endpoint definition */
  def getUsers: Endpoint[((Option[UserId], Option[Email], Option[String]), Option[Int], Option[Int]), Permission[PaginatedResult[User]]] =
    endpoint(
      request = get(url = path / "users" /? (
        qs[Option[UserId]]("userId") &
        qs[Option[Email]]("email") &
        qs[Option[String]]("apiKey") &
        qs[Option[Int]]("limit") &
        qs[Option[Int]]("page"))),
      response = jsonResponse[PaginatedResult[User]]().orForbidden(),
      tags = List("User")
    )

  /** Delete currently logged-in user endpoint definition */
  def deleteCurrentUser: Endpoint[Unit, Permission[Unit]] =
    endpoint(
      request = delete(url = path / "users" / "me"),
      response = emptyResponse().orForbidden(),
      tags = List("User")
    )

  /** Delete user endpoint definition */
  def deleteUser: Endpoint[UserId, Permission[Unit]] =
    endpoint(
      request = delete(url = path / "users" / segment[UserId]("userId")),
      response = emptyResponse().orForbidden(),
      tags = List("User")
    )

  /** User endpoint definition */
  def getUser: Endpoint[UserId, Permission[Option[User]]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("userId")),
      response = jsonResponse[User]().orNotFound().orForbidden(),
      tags = List("User")
    )
}
