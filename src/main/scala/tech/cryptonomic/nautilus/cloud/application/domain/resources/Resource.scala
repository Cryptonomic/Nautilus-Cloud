package tech.cryptonomic.nautilus.cloud.application.domain.resources

import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.Environment
import tech.cryptonomic.nautilus.cloud.application.domain.resources.Resource.ResourceId

/** Model for resource*/
case class Resource(
    resourceId: ResourceId,
    resourceName: String,
    description: String,
    platform: String,
    network: String,
    environment: Environment
)

/** Object for resource with type alias fro ResourceId */
object Resource {
  type ResourceId = Int
}
