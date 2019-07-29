package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.ApiKeySchemas
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, Environment, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/** ApiKey relevant endpoints */
trait ApiKeyEndpoints
    extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with ApiKeySchemas
    with EndpointsStatusDefinitions {

  implicit val envSegment: Segment[Environment] =
    refineSegment(stringSegment)(it => Some(Environment.byName(it)))(_.name)

  /** Currently logged-in user api keys endpoint definition */
  def getCurrentUserKeys: Endpoint[Unit, List[ApiKey]] =
    endpoint(
      request = get(url = path / "users" / "me" / "apiKeys"),
      response = jsonResponse[List[ApiKey]](),
      tags = List("User")
    )

  /** Currently logged-in user api keys usage left endpoint definition */
  def getCurrentUserUsage: Endpoint[Unit, List[UsageLeft]] =
    endpoint(
      request = get(url = path / "users" / "me" / "usage"),
      response = jsonResponse[List[UsageLeft]](),
      tags = List("User")
    )

  /** Endpoint definition for getting all ApiKeys */
  def getAllKeys: Endpoint[Unit, Permission[List[ApiKey]]] =
    endpoint(
      request = get(url = path / "apiKeys"),
      response = jsonResponse[List[ApiKey]]().orForbidden(),
      tags = List("ApiKeys")
    )

  /** Endpoint definition for validation of API Key */
  def validateApiKey: Endpoint[String, Boolean] =
    endpoint(
      request = get(url = path / "apiKeys" / segment[String]("apiKey")),
      response = jsonResponse[Boolean](),
      tags = List("ApiKeys")
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
      request = get(url = path / "users" / segment[UserId]("userId") / "usage"),
      response = jsonResponse[List[UsageLeft]]().orForbidden(),
      tags = List("User")
    )

  /** Refresh api keys endpoint definition */
  def refreshUserKeys: Endpoint[Environment, Unit] =
    endpoint(
      request = post(url = path / "users" / "me" / "apiKeys" / segment[Environment]("env") / "refresh", emptyRequest),
      response = emptyResponse(),
      tags = List("User")
    )
}
