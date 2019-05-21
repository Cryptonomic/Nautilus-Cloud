package tech.cryptonomic.cloud.nautilus.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.cloud.nautilus.domain.apiKey.ApiKey
import tech.cryptonomic.cloud.nautilus.adapters.endpoints.schemas.ApiKeySchemas

/** ApiKey relevant endpoints */
trait ApiKeyEndpoints extends algebra.Endpoints with algebra.JsonSchemaEntities with ApiKeySchemas {

  /** Endpoint definition for getting all ApiKeys */
  def getAllKeys: Endpoint[Unit, List[ApiKey]] =
    endpoint(
      request = get(url = path / "apiKeys"),
      response = jsonResponse[List[ApiKey]](),
      tags = List("ApiKeys")
    )

  /** Endpoint definition for validation of API Key */
  def validateApiKey: Endpoint[String, Boolean] =
    endpoint(
      request = get(url = path / "apiKeys" / segment[String]("apiKey")),
      response = jsonResponse[Boolean](),
      tags = List("ApiKeys")
    )

}
