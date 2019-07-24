package tech.cryptonomic.nautilus.cloud.adapters.akka

import cats.effect.IO
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.{EndpointStatusSyntax, ResourceEndpoints}
import tech.cryptonomic.nautilus.cloud.domain.ResourceService
import tech.cryptonomic.nautilus.cloud.domain.resources.CreateResource
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/** Resource routes */
class ResourceRoutes(resourceService: ResourceService[IO])
    extends ResourceEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities
    with EndpointStatusSyntax {

  /** Route for geting single resource by id */
  val getResource: Route = getResourceEndpoint.implementedByAsync { resourceId =>
    resourceService.getResource(resourceId).unsafeToFuture()
  }

  /** Route for creating resources */
  val createResource: Route = createResourceEndpoint.implementedByAsync { createResource =>
    resourceService.createResource(createResource).map(_.toString).unsafeToFuture()
  }

  /** Route for listing resources */
  val listResources: Route = listResourcesEndpoint.implementedByAsync { _ =>
    resourceService.getResources.unsafeToFuture()
  }

  /** Concatenated resource routes */
  val routes: Route = concat(
    getResource,
    createResource,
    listResources
  )

}
