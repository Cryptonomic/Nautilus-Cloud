package tech.cryptonomic.nautilus.cloud.domain.apiKey

import java.time.Instant

/** Model for creating API key */
case class CreateApiKey(
    key: String,
    resourceId: Int,
    userId: Int,
    tierId: Int,
    dateIssued: Option[Instant],
    dateSuspended: Option[Instant]
)
