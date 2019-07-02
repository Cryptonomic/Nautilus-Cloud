package tech.cryptonomic.nautilus.cloud.domain.resources

/** Model for creating resource */
case class CreateResource(resourcename: String, description: String, platform: String, network: String)
