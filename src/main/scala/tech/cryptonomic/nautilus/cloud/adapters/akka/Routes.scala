package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Directives.{
  getFromResource,
  getFromResourceDirectory,
  path,
  pathEndOrSingleSlash,
  pathPrefix,
  _
}
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings.defaultSettings
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.adapters.akka.cors.CorsConfig
import tech.cryptonomic.nautilus.cloud.adapters.akka.session.{SessionOperations, SessionRoutes}
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.Docs

class Routes(
    private val corsConfig: CorsConfig,
    private val apiKeysRoutes: ApiKeyRoutes,
    private val userRoutes: UserRoutes,
    private val sessionRoutes: SessionRoutes,
    private val resourceRoutes: ResourceRoutes,
    private val tierRoutes: TierRoutes,
    private val sessionOperations: SessionOperations
) extends StrictLogging {

  private val settings = defaultSettings.withAllowedOrigins(
    if (corsConfig.allowedOrigin == "*")
      HttpOriginMatcher.`*`
    else HttpOriginMatcher(HttpOrigin(corsConfig.allowedOrigin))
  )

  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  def getAll: Route =
    concat(
      pathPrefix("docs") {
        pathEndOrSingleSlash {
          getFromResource("web/swagger/index.html")
        }
      },
      Docs.route,
      pathPrefix("swagger-ui") {
        getFromResourceDirectory("web/swagger/swagger-ui/")
      },
      // @TODO should be removed when a proper login page is created
      path("") {
        getFromResource("web/index.html")
      },
      cors(settings) {
        concat(
          sessionRoutes.routes,
          apiKeysRoutes.getAllApiKeysForEnvRoute,
          sessionOperations.requiredSession {
            implicit session =>
              concat(
                // current routes must be at the beginning to avoid unwanted overriding (`/users/id` is being overridden by `/users/me`)
                concat(
                  userRoutes.getCurrentUserRoute,
                  apiKeysRoutes.getCurrentUserKeysRoute,
                  apiKeysRoutes.getCurrentKeyUsageRoute
                ),
                concat(
                  apiKeysRoutes.refreshKeysRoute,
                  apiKeysRoutes.getApiKeysRoute,
                  apiKeysRoutes.validateApiKeyRoute,
                  apiKeysRoutes.getUserKeysRoute,
                  apiKeysRoutes.getApiKeyUsageRoute
                ),
                concat(userRoutes.getUserRoute, userRoutes.updateUserRoute, userRoutes.deleteUserRoute),
                concat(tierRoutes.createTierRoute, tierRoutes.getTierRoute),
                concat(resourceRoutes.getResource, resourceRoutes.createResource, resourceRoutes.listResources)
              )
          }
        )
      }
    )
}
