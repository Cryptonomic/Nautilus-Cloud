package tech.cryptonomic.nautilus.cloud.application.domain.apiKey

import tech.cryptonomic.nautilus.cloud.application.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.application.domain.tier.Tier.TierId

/** Request for API key creation */
case class CreateApiKeyRequest(resourceId: ResourceId, tierId: TierId)
