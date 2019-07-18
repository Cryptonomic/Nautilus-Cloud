package tech.cryptonomic.nautilus.cloud.domain.apiKey

import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId

/** Request for API key creation */
case class CreateApiKeyRequest(resourceId: ResourceId, tierId: Int)