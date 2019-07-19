package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.EndpointStatusSyntax
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.UserEndpoints
import tech.cryptonomic.nautilus.cloud.domain.UserService
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session

// TODO:
//   users/{user}/usage	  GET	Gets the number of queries used by the given user

/** User routes implementation */
class UserRoutes(userService: UserService[IO])
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

  /** User keys route implementation */
  def getCurrentUserKeysRoute(implicit session: Session): Route = getUserKeys.implementedByAsync { userId =>
    userService.getUserApiKeys(userId).unsafeToFuture()
  }

  /** ApiKey usage route implementation */
  def getCurrentApiKeyUsageRoute(implicit session: Session): Route = getApiKeyUsage.implementedByAsync { userId =>
    userService.getUserApiKeysUsage(userId).unsafeToFuture()
  }


  /** User keys route implementation */
  def getUserKeysRoute(implicit session: Session): Route = getUserKeys.implementedByAsync { userId =>
    userService.getUserApiKeys(userId).unsafeToFuture()
  }

  /** ApiKey usage route implementation */
  def getApiKeyUsageRoute(implicit session: Session): Route = getApiKeyUsage.implementedByAsync { userId =>
    userService.getUserApiKeysUsage(userId).unsafeToFuture()
  }



  /** Concatenated User routes */
  def routes(implicit session: Session): Route = concat(
    getCurrentUserRoute,
    getUserRoute,
    getUserKeysRoute,
    updateUserRoute,
    getUserKeysRoute,
    getApiKeyUsageRoute
  )

}
