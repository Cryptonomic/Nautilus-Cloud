package tech.cryptonomic.cloud.nautilus.domain.apiKey

import java.time.Instant

/** Class representing API Key */
case class ApiKey(
    keyId: Int,
    key: String,
    resourceId: Int,
    userId: Int,
    tierId: Int,
    dateIssued: Option[Instant],
    dateSuspended: Option[Instant]
)
