package tech.cryptonomic.nautilus.cloud.domain.resources

import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import scala.language.higherKinds

/** Resource repository definition */
trait ResourceRepository[F[_]] {

  /** Creates resource */
  def createResource(cr: CreateResource): F[ResourceId]

  /** Returns all resources */
  def getResources: F[List[Resource]]

  /** Selects single resource by id */
  def getResource(resourceId: ResourceId): F[Option[Resource]]
}
