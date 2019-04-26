package tech.cryptonomic.cloud.nautilus.routes

import akka.http.scaladsl.server.Route
import endpoints.akkahttp.server
import tech.cryptonomic.cloud.nautilus.routes.endpoint.UserEndpoints
import tech.cryptonomic.cloud.nautilus.services.UserService
import akka.http.scaladsl.server.Directives._

//  users	                POST	Add new user
//  users                 PUT update user
//  users/{user}	        GET	Fetches user info
//  users/{user}/apiKeys	GET	Get all API keys for given user
//  users/{user}/usage	  GET	Gets the number of queries used by the given user
class UserRoutes(userService: UserService) extends UserEndpoints with server.Endpoints with server.JsonSchemaEntities {

  val createUserRoute: Route = createUser.implementedByAsync { userReg =>
    userService.createUser(userReg)
  }

  val updateUserRoute: Route = updateUser.implementedByAsync { user =>
    userService.updateUser(user)
  }

  val getUserRoute: Route = getUser.implementedByAsync { userId =>
    userService.getUser(userId)
  }

  val getUserKeysRoute: Route = getUserKeys.implementedByAsync { userId =>
    userService.getUserApiKeys(userId)
  }

  val routes: Route = concat(
    createUserRoute,
    updateUserRoute,
    getUserRoute,
    getUserKeysRoute
  )

}
