package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.{HttpResponse, StatusCode}
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Directives.{
  getFromResource,
  getFromResourceDirectory,
  pathEndOrSingleSlash,
  pathPrefix,
  _
}
import akka.http.scaladsl.server.{Directive0, Route}
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings.defaultSettings
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.adapters.akka.cors.CorsConfig
import tech.cryptonomic.nautilus.cloud.adapters.akka.session.{SessionOperations, SessionRoutes}
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.Docs
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session

class Routes(
    private val corsConfig: CorsConfig,
    private val apiKeysRoutes: ApiKeyRoutes,
    private val userRoutes: UserRoutes,
    private val sessionRoutes: SessionRoutes,
    private val resourceRoutes: ResourceRoutes,
    private val tierRoutes: TierRoutes,
    private val sessionOperations: SessionOperations,
    private val userActionHistoryOperations: UserActionHistoryOperations
) extends StrictLogging {

  private val settings = defaultSettings.withAllowedOrigins(
    if (corsConfig.allowedOrigin == "*")
      HttpOriginMatcher.`*`
    else HttpOriginMatcher(HttpOrigin(corsConfig.allowedOrigin))
  )

  private def validatesTosInSession(session: Session): Directive0 =
    if (session.tosAccepted) pass
    else complete(HttpResponse(StatusCode.int2StatusCode(403)))

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
      cors(settings) {
        extractClientIP {
          ip =>
            concat(
              sessionRoutes.routes,
              apiKeysRoutes.getAllApiKeysForEnvRoute,
              sessionOperations.requiredSession {
                implicit session =>
                  validatesTosInSession(session) {
                    userActionHistoryOperations.logRequestWithSession(ip)(session) {
                      concat(
                        // current routes must be at the beginning to avoid unwanted overriding (`/users/id` is being overridden by `/users/me`)
                        concat(
                          userRoutes.getCurrentUserRoute,
                          userRoutes.updateCurrentUserRoute,
                          userRoutes.getCurrentUserActions,
                          apiKeysRoutes.getCurrentUserKeysRoute,
                          apiKeysRoutes.getCurrentKeyUsageRoute,
                          apiKeysRoutes.getCurrentUserApiKeyQueryStatsRoute
                        ),
                        concat(
                          apiKeysRoutes.refreshKeysRoute,
                          apiKeysRoutes.getApiKeysRoute,
                          apiKeysRoutes.validateApiKeyRoute,
                          apiKeysRoutes.getUserKeysRoute,
                          userRoutes.getUserActions,
                          apiKeysRoutes.getApiKeyUsageRoute,
                          apiKeysRoutes.getApiKeyQueryStatsRoute,
                          apiKeysRoutes.getApiKeyAggregatedStatsRoute
                        ),
                        concat(
                          userRoutes.getUserRoute,
                          userRoutes.updateUserRoute,
                          userRoutes.deleteCurrentUserRoute,
                          userRoutes.deleteUserRoute,
                          userRoutes.getUsersRoute
                        ),
                        concat(tierRoutes.createTierRoute, tierRoutes.getTierRoute),
                        concat(
                          resourceRoutes.getResource,
                          resourceRoutes.createResource,
                          resourceRoutes.listResources
                        )
                      )
                    }
                  }

              }
            )
        }
      }
    )
}
