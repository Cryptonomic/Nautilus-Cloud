package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import endpoints.algebra.Documentation
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission

/** Utility trait for extending Endpoints functionality */
trait EndpointsUtils extends EndpointsStatusDefinitions with server.JsonSchemaEntities with StrictLogging {

  /** Extension for using Created status code */
  override def created[A](response: A => Route, invalidDocs: Documentation): A => Route = { entity =>
    complete(HttpResponse(StatusCodes.Created, entity = HttpEntity(entity.toString)))
  }

  /** Extension for using Conflict status code */
  override def conflict[A](response: A => Route, invalidDocs: Documentation): Option[A] => Route =
    _.map(response).getOrElse(complete(HttpResponse(StatusCodes.Conflict)))

  /** Extension for using Forbidden status code */
  override def forbidden[A](response: A => Route, invalidDocs: Documentation): Permission[A] => Route = {
    case Right(result) => response(result)
    case Left(permissionDenied) =>
      logger.info(permissionDenied.message)
      complete(HttpResponse(StatusCodes.Forbidden, Nil, HttpEntity(permissionDenied.message)))
  }
}
