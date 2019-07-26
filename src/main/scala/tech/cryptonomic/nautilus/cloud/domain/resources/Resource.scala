package tech.cryptonomic.nautilus.cloud.domain.resources

import tech.cryptonomic.nautilus.cloud.domain.apiKey.Environment
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId

/** Model for resource*/
case class Resource(
    resourceid: ResourceId,
    resourcename: String,
    description: String,
    platform: String,
    network: String,
    environment: Environment
)

/** Object for resource with type alias fro ResourceId */
object Resource {
  type ResourceId = Int
}
