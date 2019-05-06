package tech.cryptonomic.cloud.nautilus.routes

import akka.http.scaladsl.server.Route
import endpoints.akkahttp.server
import tech.cryptonomic.cloud.nautilus.routes.endpoint.UserEndpoints
import tech.cryptonomic.cloud.nautilus.services.UserService
import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import tech.cryptonomic.cloud.nautilus.model.UsageLeft

import scala.concurrent.Future

// TODO:
//   users/{user}/usage	  GET	Gets the number of queries used by the given user

/** User routes implementation */
class UserRoutes(userService: UserService[IO])
    extends UserEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities {

  /** User creation route implementation */
  val createUserRoute: Route = createUser.implementedByAsync { userReg =>
    userService.createUser(userReg).unsafeToFuture()
  }

  /** User update route implementation */
  val updateUserRoute: Route = updateUser.implementedByAsync { user =>
    userService.updateUser(user).unsafeToFuture()
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
  val getApiKeyUsageRoute: Route = getApiKeyUsage.implementedByAsync { apiKey =>
    Future.successful(Some(UsageLeft("dummyKey", 500, 15000)))
  }

  /** Concatenated User routes */
  val routes: Route = concat(
    createUserRoute,
    updateUserRoute,
    getUserRoute,
    getUserKeysRoute
  )

}
