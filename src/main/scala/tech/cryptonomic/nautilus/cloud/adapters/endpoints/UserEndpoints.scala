package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.UserSchemas
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, CreateApiKeyRequest, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{UpdateUser, User}

/** User relevant endpoints */
trait UserEndpoints
    extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with UserSchemas
    with EndpointsStatusDefinitions {

  /** User update endpoint definition */
  def updateUser: Endpoint[(UserId, UpdateUser), Permission[Unit]] =
    endpoint(
      request = put(url = path / "users" / segment[Int]("userId"), jsonRequest[UpdateUser]()),
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

  /** User endpoint definition */
  def getUser: Endpoint[UserId, Permission[Option[User]]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("userId")),
      response = jsonResponse[User]().orNotFound().orForbidden(),
      tags = List("User")
    )

  /** User keys endpoint definition */
  def getUserKeys: Endpoint[UserId, List[ApiKey]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("userId") / "apiKeys"),
      response = jsonResponse[List[ApiKey]](),
      tags = List("User")
    )

  /** Api keys endpoint definition */
  def getApiKeyUsage: Endpoint[UserId, List[UsageLeft]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("user") / "usage"),
      response = jsonResponse[List[UsageLeft]](),
      tags = List("User")
    )

  /** Issues an api key for an user */
  def issueApiKey: Endpoint[(UserId, CreateApiKeyRequest), Option[String]] =
    endpoint(
      request = post(
        url = path / "users" /segment[UserId]("userId") / "apiKeys",
        entity = jsonRequest[CreateApiKeyRequest]()
      ),
      response = textResponse().withCreatedStatus().orNotFound(),
      tags = List("User")
    )
}
