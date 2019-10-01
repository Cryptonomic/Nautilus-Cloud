package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.EndpointStatusSyntax
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.UserEndpoints
import tech.cryptonomic.nautilus.cloud.application.{ApiKeyApplication, UserApplication}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session

/** User routes implementation */
class UserRoutes(userApplication: UserApplication[IO], apiKeyApplication: ApiKeyApplication[IO])
    extends UserEndpoints
    with server.Endpoints
    with EndpointStatusSyntax
    with StrictLogging {

  /** User update route implementation */
  def updateUserRoute(implicit session: Session): Route = updateUser.implementedByAsync {
    case (userId, user) =>
      userApplication.updateUser(userId, user).unsafeToFuture()
  }

  /** User route implementation */
  def getUserRoute(implicit session: Session): Route = getUser.implementedByAsync { userId =>
    userApplication.getUser(userId).unsafeToFuture()
  }

  /** Current user route implementation */
  def getCurrentUserRoute(implicit session: Session): Route = getCurrentUser.implementedByAsync { _ =>
    userApplication.getCurrentUser.unsafeToFuture()
  }

  /** All users route implementation */
  def getAllUsersRoute(implicit session: Session): Route = getAllUsers.implementedByAsync { _ =>
    userApplication.getAllUsers.unsafeToFuture()
  }

  /** Delete current user route implementation */
  def deleteUserRoute(implicit userSession: Session): Route = deleteCurrentUser.implementedByAsync { _ =>
    userApplication.deleteCurrentUser.unsafeToFuture()
  }
}
