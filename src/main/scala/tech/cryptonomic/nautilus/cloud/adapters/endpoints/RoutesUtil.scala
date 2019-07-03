package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import endpoints.algebra.Documentation

/** Util method implementations for routes */
trait RoutesUtil {

  /** Extension for using Created status code */
  def created[A](response: A => Route, invalidDocs: Documentation): A => Route = { entity =>
    complete(HttpResponse(StatusCodes.Created, entity = HttpEntity(entity.toString)))
  }

  /** Extension for using Conflict status code */
  def conflict[A](response: A => Route, invalidDocs: Documentation): Option[A] => Route =
    _.map(response).getOrElse(complete(HttpResponse(StatusCodes.Conflict)))

}
