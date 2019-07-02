package tech.cryptonomic.nautilus.cloud.domain.resources

/** Model for resource*/
case class Resource(resourceid: Int, resourcename: String, description: String, platform: String, network: String)

/** Object for resource with type alias fro ResourceId */
object Resource {
  type ResourceId = Int
}
