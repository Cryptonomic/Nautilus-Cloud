package tech.cryptonomic.nautilus.cloud.application

import tech.cryptonomic.nautilus.cloud.application.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.application.domain.resources.{CreateResource, Resource, ResourceRepository, ResourceService}

import scala.language.higherKinds

/** Service for handling resources operations */
class ResourceApplication[F[_]](resourceService: ResourceService[F]) {

  /** Creates new resource */
  def createResource(createResource: CreateResource): F[ResourceId] =
    resourceService.createResource(createResource)

  /** Returns all resources */
  def getResources: F[List[Resource]] =
    resourceService.getResources

  /** Returns single resource by id */
  def getResource(resourceId: ResourceId): F[Option[Resource]] =
    resourceService.getResource(resourceId)
}
