package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.UserSchemas
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{UpdateUser, User}

/** User relevant endpoints */
trait UserEndpoints extends algebra.Endpoints with algebra.JsonSchemaEntities with UserSchemas with EndpointsUtil {

  /** User update endpoint definition */
  def updateUser: Endpoint[(UserId, UpdateUser), Unit] =
    endpoint(
      request = put(url = path / "users" / segment[Int]("userId"), jsonRequest[UpdateUser]()),
      response = emptyResponse().withCreatedStatus(),
      tags = List("User")
    )

  /** Current user endpoint definition */
  def getCurrentUser: Endpoint[Unit, Option[User]] =
    endpoint(
      request = get(url = path / "users" / "me"),
      response = jsonResponse[User]().orNotFound(),
      tags = List("User")
    )

  /** User endpoint definition */
  def getUser: Endpoint[UserId, Option[User]] =
    endpoint(
      request = get(url = path / "users" / segment[UserId]("userId")),
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
