package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import endpoints.akkahttp.server
import endpoints.algebra.Documentation
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.{UsageLeft, UserEndpoints}
import tech.cryptonomic.nautilus.cloud.domain.UserService

// TODO:
//   users/{user}/usage	  GET	Gets the number of queries used by the given user

/** User routes implementation */
class UserRoutes(userService: UserService[IO])
    extends UserEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities {

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
  val getApiKeyUsageRoute: Route = getApiKeyUsage.implementedBy { apiKey =>
    Some(UsageLeft("dummyKey", 500, 15000))
  }

  /** Concatenated User routes */
  val routes: Route = concat(
    createUserRoute,
    updateUserRoute,
    getUserRoute,
    getUserKeysRoute
  )

  /** Extension for using Created status code */
  override def created[A](response: A => Route, invalidDocs: Documentation): A => Route = { entity =>
    complete(HttpResponse(StatusCodes.Created, entity = HttpEntity(entity.toString)))
  }

  /** Extension for using Conflict status code */
  override def conflict[A](response: A => Route, invalidDocs: Documentation): Option[A] => Route =
    _.map(response).getOrElse(complete(HttpResponse(StatusCodes.Conflict)))
}
