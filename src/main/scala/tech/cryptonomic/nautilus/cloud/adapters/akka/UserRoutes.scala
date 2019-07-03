package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.{RoutesUtil, UserEndpoints}
import tech.cryptonomic.nautilus.cloud.domain.UserService

// TODO:
//   users/{user}/usage	  GET	Gets the number of queries used by the given user

/** User routes implementation */
class UserRoutes(userService: UserService[IO])
    extends UserEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities
    with RoutesUtil {

  /** User creation route implementation */
  val createUserRoute: Route = createUser.implementedByAsync { userReg =>
    userService.createUser(userReg).map(_.toOption.map(_.toString)).unsafeToFuture()
  }

  /** User update route implementation */
  val updateUserRoute: Route = updateUser.implementedByAsync {
    case (userId, user) =>
      userService.updateUser(userId, user).unsafeToFuture()
  }

  /** User route implementation */
  val getUserRoute: Route = getUser.implementedByAsync { userId =>
    userService.getUser(userId).unsafeToFuture()
  }

  /** User keys route implementation */
  val getUserKeysRoute: Route = getUserKeys.implementedByAsync { userId =>
    userService.getUserApiKeys(userId).unsafeToFuture()
  }

  /** ApiKey usage route implementation */
  val getApiKeyUsageRoute: Route = getApiKeyUsage.implementedByAsync { userId =>
    userService.getUserApiKeysUsage(userId).unsafeToFuture()
  }

  /** Concatenated User routes */
  val routes: Route = concat(
    createUserRoute,
    updateUserRoute,
    getUserRoute,
    getUserKeysRoute
  )

}
