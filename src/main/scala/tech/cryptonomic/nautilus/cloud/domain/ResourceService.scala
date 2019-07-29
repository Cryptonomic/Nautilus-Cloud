package tech.cryptonomic.nautilus.cloud.domain

import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource, ResourceRepository}

import scala.language.higherKinds

/** Service for handling resources operations */
class ResourceService[F[_]](resourcesRepo: ResourceRepository[F]) {

  /** Creates new resource */
  def createResource(createResource: CreateResource): F[ResourceId] =
    resourcesRepo.createResource(createResource)

  /** Returns all resources */
  def getResources: F[List[Resource]] =
    resourcesRepo.getResources

  /** Returns single resource by id */
  def getResource(resourceId: ResourceId): F[Option[Resource]] =
    resourcesRepo.getResource(resourceId)

}
