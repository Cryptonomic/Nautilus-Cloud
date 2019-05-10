package tech.cryptonomic.cloud.nautilus.routes.endpoint

import endpoints.algebra
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, UsageLeft, User, UserWithoutId}
import tech.cryptonomic.cloud.nautilus.routes.endpoint.schemas.UserSchemas

/** User relevant endpoints */
trait UserEndpoints extends algebra.Endpoints with algebra.JsonSchemaEntities with UserSchemas with EndpointsUtil {

  /** User creation endpoint definition */
  def createUser: Endpoint[UserWithoutId, String] =
    endpoint(
      request = post(url = path / "users", jsonRequest[UserWithoutId]()),
      response = textResponse(Some("User created!")).withCreatedStatus(),
      tags = List("User")
    )

  /** User update endpoint definition */
  def updateUser: Endpoint[(Int, UserWithoutId), Unit] =
    endpoint(
      request = put(url = path / "users" / segment[Int]("userId"), jsonRequest[UserWithoutId]()),
      response = emptyResponse(Some("User updated!")).withCreatedStatus(),
      tags = List("User")
    )

  /** User endpoint definition */
  def getUser: Endpoint[Int, Option[User]] =
    endpoint(
      request = get(url = path / "users" / segment[Int]("userId")),
      response = jsonResponse[User]().orNotFound(),
      tags = List("User")
    )

  /** User keys endpoint definition */
  def getUserKeys: Endpoint[Int, List[ApiKey]] =
    endpoint(
      request = get(url = path / "users" / segment[Int]("userId") / "apiKeys"),
      response = jsonResponse[List[ApiKey]](),
      tags = List("User")
    )

  def getApiKeyUsage: Endpoint[String, Option[UsageLeft]] =
    endpoint(
      request = get(url = path / "users" / segment[String]("apiKey") / "usage"),
      response = jsonResponse[UsageLeft]().orNotFound(),
      tags = List("User")
    )
}
