package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.UserSchemas
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, Environment, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{UpdateUser, User}

/** User relevant endpoints */
trait UserEndpoints
    extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with UserSchemas
    with EndpointsStatusDefinitions {

  implicit val envSegment: Segment[Environment] =
    refineSegment(stringSegment)(it => Some(Environment.byName(it)))(_.name)

  /** User update endpoint definition */
  def updateUser: Endpoint[(UserId, UpdateUser), Permission[Unit]] =
    endpoint(
      request = put(url = path / "users" / segment[UserId]("userId"), jsonRequest[UpdateUser]()),
      response = emptyResponse().withCreatedStatus().orForbidden(),
      tags = List("User")
    )

  /** Currently logged-in user endpoint definition */
  def getCurrentUser: Endpoint[Unit, Option[User]] =
    endpoint(
      request = get(url = path / "users" / "me"),
      response = jsonResponse[User]().orNotFound(),
      tags = List("User")
    )

  /** Currently logged-in user api keys endpoint definition */
  def getCurrentUserKeys: Endpoint[Unit, List[ApiKey]] =
    endpoint(
      request = get(url = path / "users" / "me" / "apiKeys"),
      response = jsonResponse[List[ApiKey]](),
      tags = List("User")
    )

  /** Refresh api keys endpoint definition */
  def refreshUserKeys: Endpoint[Environment, Unit] =
    endpoint(
      request = post(url = path / "users" / "me" / "apiKeys" / segment[Environment]("env") / "refresh", emptyRequest),
      response = emptyResponse(),
      tags = List("User")
    )

  /** Currently logged-in user api keys usage left endpoint definition */
  def getCurrentUserUsage: Endpoint[Unit, List[UsageLeft]] =
    endpoint(
      request = get(url = path / "users" / "me" / "usage"),
      response = jsonResponse[List[UsageLeft]](),
      tags = List("User")
    )

  /** User endpoint definition */
  def getUser: Endpoint[UserId, Permission[Option[User]]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("userId")),
      response = jsonResponse[User]().orNotFound().orForbidden(),
      tags = List("User")
    )

  /** User keys endpoint definition */
  def getUserKeys: Endpoint[UserId, Permission[List[ApiKey]]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("userId") / "apiKeys"),
      response = jsonResponse[List[ApiKey]]().orForbidden(),
      tags = List("User")
    )

  /** Api keys usage endpoint definition */
  def getApiKeyUsage: Endpoint[UserId, Permission[List[UsageLeft]]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("user") / "usage"),
      response = jsonResponse[List[UsageLeft]]().orForbidden(),
      tags = List("User")
    )

}
