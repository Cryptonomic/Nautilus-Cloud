package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.ResourceSchemas
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource}
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId

/** Resource endpoints definition*/
trait ResourceEndpoints
    extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with ResourceSchemas
    with EndpointsStatusDefinitions {

  /** Resource endpoint */
  def getResourceEndpoint: Endpoint[Int, Option[Resource]] =
    endpoint(
      request = get(url = path / "resources" / segment[ResourceId]("resourceId")),
      response = jsonResponse[Resource]().orNotFound(),
      tags = List("Resource")
    )

  /** Resource creation endpoint */
  def createResourceEndpoint: Endpoint[CreateResource, String] =
    endpoint(
      request = post(url = path / "resources", entity = jsonRequest[CreateResource]()),
      response = textResponse().withCreatedStatus(),
      tags = List("Resource")
    )

  /** Resources list endpoint */
  def listResourcesEndpoint: Endpoint[Unit, List[Resource]] =
    endpoint(
      request = get(url = path / "resources"),
      response = jsonResponse[List[Resource]](),
      tags = List("Resource")
    )

}
