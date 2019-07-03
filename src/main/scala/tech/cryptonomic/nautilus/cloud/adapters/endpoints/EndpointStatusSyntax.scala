package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.Complete
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import endpoints.algebra.Documentation
import tech.cryptonomic.nautilus.cloud.adapters.akka.Mappers.ResponseMapper
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission

/** Utility trait for extending Endpoints functionality */
trait EndpointStatusSyntax extends EndpointsStatusDefinitions with server.JsonSchemaEntities with StrictLogging {

  /** Extension for using Created status code */
  override def created[A](response: A => Route, invalidDocs: Documentation): A => Route = response.embeddedMap {
    case Complete(httpResponse) => Complete(httpResponse.withStatus(StatusCodes.Created))
    case it => it
  }

  /** Extension for using Conflict status code */
  override def conflict[A](response: A => Route, invalidDocs: Documentation): Option[A] => Route =
    _.map(response).getOrElse(complete(HttpResponse(StatusCodes.Conflict)))

  /** Extension for using Bad request status code */
  override def badRequest[A](response: A => Route, invalidDocs: Documentation): Either[Throwable, A] => Route = {
    case Right(result) => response(result)
    case Left(throwable) =>
      complete(HttpResponse(StatusCodes.BadRequest, Nil, HttpEntity(throwable.getMessage)))
  }

  /** Extension for using Forbidden status code */
  override def forbidden[A](response: A => Route, invalidDocs: Documentation): Permission[A] => Route = {
    case Right(result) => response(result)
    case Left(permissionDenied) =>
      logger.info(permissionDenied.message)
      complete(HttpResponse(StatusCodes.Forbidden, Nil, HttpEntity(permissionDenied.message)))
  }
}
