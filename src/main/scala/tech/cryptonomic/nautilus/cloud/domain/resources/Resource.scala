package tech.cryptonomic.nautilus.cloud.domain.resources

import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId

/** Model for resource*/
case class Resource(
    resourceid: ResourceId,
    resourcename: String,
    description: String,
    platform: String,
    network: String
)
//('Tezos Alphanet Conseil Dev', 'Conseil alphanet development environment', 'tezos', 'alphanet');
//('Tezos Mainnet Conseil Dev', 'Conseil mainnet development environment', 'tezos', 'mainnet');
//('Tezos Alphanet Conseil Prod', 'Conseil alphanet production environment', 'tezos', 'alphanet');
//('Tezos Mainnet Conseil Prod', 'Conseil mainnet production environment', 'tezos', 'mainnet');

/** Object for resource with type alias fro ResourceId */
object Resource {
    type ResourceId = Int
    val defaultTezosDevAlphanetId = 1
    val defaultTezosDevMainnetId = 2
    val defaultTezosProdAlphanetId = 3
    val defaultTezosProdMainnetId = 4
}
