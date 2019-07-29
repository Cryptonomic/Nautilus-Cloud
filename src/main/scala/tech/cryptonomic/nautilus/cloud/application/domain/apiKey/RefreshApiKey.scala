package tech.cryptonomic.nautilus.cloud.application.domain.apiKey

import java.time.Instant

case class RefreshApiKey(
    userId: Int,
    environment: Environment,
    apiKey: String,
    now: Instant
)
