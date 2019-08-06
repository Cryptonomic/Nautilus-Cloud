package tech.cryptonomic.nautilus.cloud.domain.resources
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import io.scalaland.chimney.dsl._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.Environment

/** Model for creating resource */
case class CreateResource(
    resourceName: String,
    description: String,
    platform: String,
    network: String,
    environment: Environment
) {

  /** Transforms into Resource with given resourceId */
  def toResource(resourceId: ResourceId): Resource =
    this
      .into[Resource]
      .withFieldConst(_.resourceId, resourceId)
      .transform
}
