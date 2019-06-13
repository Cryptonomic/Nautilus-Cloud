package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.UserSchemas
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User}

/** User relevant endpoints */
trait UserEndpoints extends algebra.Endpoints with algebra.JsonSchemaEntities with UserSchemas with EndpointsUtil {

  /** User update endpoint definition */
  def updateUser: Endpoint[(Int, UpdateUser), Unit] =
    endpoint(
      request = put(url = path / "users" / segment[Int]("userId"), jsonRequest[UpdateUser]()),
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

  /** Api keys endpoint definition */
  def getApiKeyUsage: Endpoint[String, Option[UsageLeft]] =
    endpoint(
      request = get(url = path / "users" / segment[String]("apiKey") / "usage"),
      response = jsonResponse[UsageLeft]().orNotFound(),
      tags = List("User")
    )
}
