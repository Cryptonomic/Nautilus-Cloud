package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.EndpointStatusSyntax
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.UserEndpoints
import tech.cryptonomic.nautilus.cloud.application.{ApiKeyApplication, UserApplication}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.pagination.Pagination

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

  /** Current user update route implementation */
  def updateCurrentUserRoute(implicit session: Session): Route = updateCurrentUser.implementedByAsync(user =>
    userApplication.updateCurrentUser(user).unsafeToFuture())

  /** User route implementation */
  def getUserRoute(implicit session: Session): Route = getUser.implementedByAsync { userId =>
    userApplication.getUser(userId).unsafeToFuture()
  }

  /** Current user route implementation */
  def getCurrentUserRoute(implicit session: Session): Route = getCurrentUser.implementedByAsync { _ =>
    userApplication.getCurrentUser.unsafeToFuture()
  }

  /** Users route implementation */
  def getUsersRoute(implicit session: Session): Route = getUsers.implementedByAsync {
    case ((userId, email, apiKey), limit, page) =>
      userApplication.getUsers(userId, email, apiKey)(Pagination(limit, page)).unsafeToFuture()
  }

  /** Delete current user route implementation */
  def deleteCurrentUserRoute(implicit userSession: Session): Route = deleteCurrentUser.implementedByAsync { _ =>
    userApplication.deleteCurrentUser.unsafeToFuture()
  }

  def deleteUserRoute(implicit userSession: Session): Route = deleteUser.implementedByAsync { userId =>
    userApplication.deleteUser(userId).unsafeToFuture()
  }
}
