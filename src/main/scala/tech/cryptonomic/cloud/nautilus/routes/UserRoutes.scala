package tech.cryptonomic.cloud.nautilus.routes

import akka.http.scaladsl.server.Route
import endpoints.akkahttp.server
import tech.cryptonomic.cloud.nautilus.routes.endpoint.UserEndpoints
import tech.cryptonomic.cloud.nautilus.services.UserService
import akka.http.scaladsl.server.Directives._
import cats.effect.IO

// TODO:
//   users/{user}/usage	  GET	Gets the number of queries used by the given user
class UserRoutes(userService: UserService[IO])
    extends UserEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities {

  val createUserRoute: Route = createUser.implementedByAsync { userReg =>
    userService.createUser(userReg).unsafeToFuture()
  }

  val updateUserRoute: Route = updateUser.implementedByAsync { user =>
    userService.updateUser(user).unsafeToFuture()
  }

  val getUserRoute: Route = getUser.implementedByAsync { userId =>
    userService.getUser(userId).unsafeToFuture()
  }

  val getUserKeysRoute: Route = getUserKeys.implementedByAsync { userId =>
    userService.getUserApiKeys(userId).unsafeToFuture()
  }

  val routes: Route = concat(
    createUserRoute,
    updateUserRoute,
    getUserRoute,
    getUserKeysRoute
  )

}
