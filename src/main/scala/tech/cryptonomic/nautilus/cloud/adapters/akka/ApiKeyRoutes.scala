package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.ApiKeyEndpoints
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.EndpointStatusSyntax
import tech.cryptonomic.nautilus.cloud.domain.ApiKeyService
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session

/** API Keys routes implementation */
class ApiKeyRoutes(apiKeysService: ApiKeyService[IO])
    extends ApiKeyEndpoints
    with server.Endpoints
    with EndpointStatusSyntax
    with StrictLogging {

  /** Routes implementation for getting all ApiKeys */
  def getAllApiKeysRoute(implicit session: Session): Route = getAllKeys.implementedByAsync { _ =>
    apiKeysService.getAllApiKeys.unsafeToFuture()
  }

  /** Routes implementation for getting all ApiKeys */
  val getAllApiKeysForEnvRoute: Route = getAllKeysForEnv.implementedByAsync { case (env, key) =>
    apiKeysService.getAllApiKeysForEnv(key, env).unsafeToFuture()
  }

  /** Routes implementation for validation of API Key */
  val validateApiKeyRoute: Route =
    validateApiKey.implementedByAsync { apiKey =>
      apiKeysService.validateApiKey(apiKey).unsafeToFuture()
    }

  /** Concatenated API keys routes */
  def routes(implicit session: Session): Route = concat(
    getAllApiKeysRoute,
    validateApiKeyRoute
  )
}
