package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.EndpointStatusSyntax
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.UserEndpoints
import tech.cryptonomic.nautilus.cloud.application.{ApiKeyApplication, UserApplication}
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.Session

/** User routes implementation */
class UserRoutes(userService: UserApplication[IO], apiKeyService: ApiKeyApplication[IO])
    extends UserEndpoints
    with server.Endpoints
    with EndpointStatusSyntax
    with StrictLogging {

  /** User update route implementation */
  def updateUserRoute(implicit session: Session): Route = updateUser.implementedByAsync {
    case (userId, user) =>
      userService.updateUser(userId, user).unsafeToFuture()
  }

  /** User route implementation */
  def getUserRoute(implicit session: Session): Route = getUser.implementedByAsync { userId =>
    userService.getUser(userId).unsafeToFuture()
  }

  /** Current user route implementation */
  def getCurrentUserRoute(implicit session: Session): Route = getCurrentUser.implementedByAsync { _ =>
    userService.getCurrentUser.unsafeToFuture()
  }
}
