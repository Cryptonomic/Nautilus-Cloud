package tech.cryptonomic.nautilus.cloud.domain.resources
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import io.scalaland.chimney.dsl._

/** Model for creating resource */
case class CreateResource(resourceName: String, description: String, platform: String, network: String) {
  /** Transforms into Resource with given resourceId */
  def toResource(resourceId: ResourceId): Resource =
    this.into[Resource].withFieldConst(_.resourceid, resourceId).withFieldRenamed(_.resourceName, _.resourcename).transform
}
