package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import endpoints.akkahttp.server
import endpoints.algebra.Documentation
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.{UsageLeft, UserEndpoints}
import tech.cryptonomic.nautilus.cloud.domain.UserService
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session

// TODO:
//   users/{user}/usage	  GET	Gets the number of queries used by the given user

/** User routes implementation */
class UserRoutes(userService: UserService[IO])
    extends UserEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities {

  /** User update route implementation */
  def updateUserRoute(session: Session): Route = updateUser.implementedByAsync {
    case (userId, user) =>
      userService.updateUser(session)(userId, user).unsafeToFuture()
  }

  /** User route implementation */
  def getUserRoute(session: Session): Route = getUser.implementedByAsync { userId =>
    userService.getUser(session)(userId).unsafeToFuture()
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
  def routes(session: Session): Route = concat(
    updateUserRoute(session),
    getUserRoute(session),
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
