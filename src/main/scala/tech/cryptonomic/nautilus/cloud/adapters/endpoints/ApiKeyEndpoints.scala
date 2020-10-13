package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.ApiKeySchemas
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.AggregatedMeteringStats
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

  /** Endpoint definition for getting all ApiKeys */
  def getAllKeysForEnv: Endpoint[(Environment, String), Permission[List[String]]] =
    endpoint(
      request = get(url = path / "apiKeys" / segment[Environment]("environment"), headers = header("X-Api-Key")),
      response = jsonResponse[List[String]]().orForbidden(),
      tags = List("ApiKeys")
    )

  /** Endpoint definition for validation of API Key */
  def validateApiKey: Endpoint[String, Boolean] =
    endpoint(
      request = get(url = path / "apiKeys" / segment[String]("apiKey") / "valid"),
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
  def refreshKeys: Endpoint[Environment, ApiKey] =
    endpoint(
      request = post(url = path / "users" / "me" / "apiKeys" / segment[Environment]("env") / "refresh", emptyRequest),
      response = jsonResponse[ApiKey](),
      tags = List("User")
    )

  /** Api keys usage endpoint definition */
  def getCurrentUserApiKeyStats: Endpoint[Unit, MeteringStats] =
    endpoint(
      request = get(url = path / "users" / "me" / "stats"),
      response = jsonResponse[MeteringStats](),
      tags = List("User")
    )

  /** Api keys aggregated stats endpoint definition */
  def getApiKeyAggregatedStats: Endpoint[(UserId, Option[Long], Option[String]), Permission[
    List[AggregatedMeteringStats]
  ]] =
    endpoint(
      request = get(
        url = path / "users" / segment[UserId]("userId") / "stats" / "aggregated" /? qs[Option[Long]]("from"),
        headers = optHeader("X-Api-Key")
      ),
      response = jsonResponse[List[AggregatedMeteringStats]]().orForbidden(),
      tags = List("User")
    )

  /** Refresh api keys endpoint definition */
  def activateApiKeysForUsers: Endpoint[(List[UserId], String), Permission[Unit]] =
    endpoint(
      request = post(
        url = path / "users" / "all" / "apiKeys" / "activate",
        entity = jsonRequest[List[UserId]](),
        headers = header("X-Api-Key")
      ),
      response = emptyResponse().orForbidden(),
      tags = List("ApiKeys")
    )

  /** Refresh api keys endpoint definition */
  def deactivateApiKeysForUsers: Endpoint[(List[UserId], String), Permission[Unit]] =
    endpoint(
      request = post(
        url = path / "users" / "all" / "apiKeys" / "deactivate",
        entity = jsonRequest[List[UserId]](),
        headers = header("X-Api-Key")
      ),
      response = emptyResponse().orForbidden(),
      tags = List("ApiKeys")
    )
}
