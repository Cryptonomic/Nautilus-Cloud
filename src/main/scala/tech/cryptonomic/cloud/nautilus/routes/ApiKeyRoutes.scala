package tech.cryptonomic.cloud.nautilus.routes

import akka.http.scaladsl.server.Route
import endpoints.akkahttp.server
import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import tech.cryptonomic.cloud.nautilus.routes.endpoint.ApiKeyEndpoints
import tech.cryptonomic.cloud.nautilus.services.ApiKeyService

/** API Keys routes implementation */
class ApiKeyRoutes(apiKeysService: ApiKeyService[IO])
    extends ApiKeyEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities {

  /** Routes implementation for getting all ApiKeys */
  val getAllApiKeysRoute: Route = getAllKeys.implementedByAsync { _ =>
    apiKeysService.getAllApiKeys.unsafeToFuture()
  }

  /** Routes implementation for validation of API Key */
  val validateApiKeyRoute: Route =
    validateApiKey.implementedByAsync { apiKey =>
      apiKeysService.validateApiKey(apiKey).unsafeToFuture()
    }

  /** Concatenated API keys routes */
  val routes: Route = concat(
    getAllApiKeysRoute,
    validateApiKeyRoute
  )

}
