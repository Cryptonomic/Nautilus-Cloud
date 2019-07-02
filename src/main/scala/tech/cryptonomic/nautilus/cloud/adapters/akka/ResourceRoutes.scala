package tech.cryptonomic.nautilus.cloud.adapters.akka

import cats.effect.IO
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.ResourceEndpoints
import tech.cryptonomic.nautilus.cloud.domain.ResourceService
import tech.cryptonomic.nautilus.cloud.domain.resources.CreateResource
import akka.http.scaladsl.server.Directives._

/** Resource routes */
class ResourceRoutes(resourceService: ResourceService[IO])
    extends ResourceEndpoints
    with server.Endpoints
    with server.JsonSchemaEntities {

  /** Route for geting single resource by id */
  private val getResource = getResourceEndpoint.implementedByAsync { resourceId =>
    resourceService.getResource(resourceId).unsafeToFuture()
  }

  /** Route for creating resources */
  private val createResource = createResourceEndpoint.implementedByAsync { createResource: CreateResource =>
    resourceService.createResource(createResource).map(_.toString).unsafeToFuture()
  }

  /** Route for listing resources */
  private val listResources = listResourcesEndpoint.implementedByAsync { _ =>
    resourceService.getResources.unsafeToFuture()
  }

  /** Concatenated resource routes */
  val routes = concat(
    getResource,
    createResource,
    listResources
  )

}
