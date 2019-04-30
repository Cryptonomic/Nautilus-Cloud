package tech.cryptonomic.cloud.nautilus.routes

import akka.http.scaladsl.server.Route
import endpoints.akkahttp.server
import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import tech.cryptonomic.cloud.nautilus.routes.endpoint.ApiKeyEndpoints
import tech.cryptonomic.cloud.nautilus.services.ApiKeyService

class ApiKeyRoutes(apiKeysService: ApiKeyService[IO])
    extends ApiKeyEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities {

  val getAllApiKeysRoute: Route = getAllKeys.implementedByAsync { _ =>
    apiKeysService.getAllApiKeys.unsafeToFuture()
  }

  val validateApiKeyRoute: Route =
    validateApiKey.implementedByAsync { apiKey =>
      apiKeysService.validateApiKey(apiKey).unsafeToFuture()
    }

  val routes: Route = concat(
    getAllApiKeysRoute,
    validateApiKeyRoute
  )

}
