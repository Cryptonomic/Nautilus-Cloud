package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.StatusCodes.Found
import akka.http.scaladsl.server.Directives.{
  getFromResource,
  getFromResourceDirectory,
  path,
  pathEndOrSingleSlash,
  pathPrefix,
  redirect,
  _
}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.adapters.akka.session.{SessionOperations, SessionRoutes}
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.Docs
import tech.cryptonomic.nautilus.cloud.domain.user.Role

class Routes(
    private val apiKeysRoutes: ApiKeyRoutes,
    private val userRoutes: UserRoutes,
    private val sessionRoutes: SessionRoutes,
    private val resourceRoutes: ResourceRoutes,
    private val tierRoutes: TierRoutes,
    private val sessionOperations: SessionOperations
) extends StrictLogging {

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
      pathPrefix("site") {
        getFromResource("web/index.html")
      },
      path("") {
        redirect("/site", Found)
      },
      sessionRoutes.routes,
      apiKeysRoutes.getAllApiKeysForEnvRoute,
      sessionOperations.requiredSession { implicit session =>
        concat(
          apiKeysRoutes.routes,
          userRoutes.routes,
          tierRoutes.routes,
          resourceRoutes.routes
        )
      }
    )
}
