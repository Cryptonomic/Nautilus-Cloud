package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.ApiKeyEndpoints
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.EndpointStatusSyntax
import tech.cryptonomic.nautilus.cloud.application.ApiKeyApplication
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.Session

/** API Keys routes implementation */
class ApiKeyRoutes(apiKeysApplication: ApiKeyApplication[IO])
    extends ApiKeyEndpoints
    with server.Endpoints
    with EndpointStatusSyntax
    with StrictLogging {

  /** Routes implementation for getting all ApiKeys */
  def getAllApiKeysRoute(implicit session: Session): Route = getAllKeys.implementedByAsync { _ =>
    apiKeysApplication.getAllApiKeys.unsafeToFuture()
  }

  /** Routes implementation for getting all ApiKeys */
  val getAllApiKeysForEnvRoute: Route = getAllKeysForEnv.implementedByAsync { case (env, key) =>
    apiKeysApplication.getApiKeysForEnv(key, env).unsafeToFuture()
  }

  /** Routes implementation for validation of API Key */
  val validateApiKeyRoute: Route =
    validateApiKey.implementedByAsync { apiKey =>
      apiKeysApplication.validateApiKey(apiKey).unsafeToFuture()
    }

  /** User keys route implementation */
  def getCurrentUserKeysRoute(implicit session: Session): Route = getCurrentUserKeys.implementedByAsync { _ =>
    apiKeysApplication.getCurrentUserApiKeys.unsafeToFuture()
  }

  /** User keys refresh implementation */
  def refreshKeysRoute(implicit session: Session): Route = refreshUserKeys.implementedByAsync { env =>
    apiKeysApplication.refreshApiKey(env).unsafeToFuture()
  }

  /** ApiKey usage route implementation */
  def getCurrentApiKeyUsageRoute(implicit session: Session): Route = getCurrentUserUsage.implementedByAsync { _ =>
    apiKeysApplication.getCurrentUserApiKeysUsage.unsafeToFuture()
  }

  /** User keys route implementation */
  def getUserKeysRoute(implicit session: Session): Route = getUserKeys.implementedByAsync { userId =>
    apiKeysApplication.getApiKeys(userId).unsafeToFuture()
  }

  /** ApiKey usage route implementation */
  def getApiKeyUsageRoute(implicit session: Session): Route = getApiKeyUsage.implementedByAsync { userId =>
    apiKeysApplication.getUserApiKeysUsage(userId).unsafeToFuture()
  }
}
