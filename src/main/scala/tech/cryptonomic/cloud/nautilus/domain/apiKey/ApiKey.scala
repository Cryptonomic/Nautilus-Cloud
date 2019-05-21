package tech.cryptonomic.cloud.nautilus.domain.apiKey

import java.sql.Timestamp

/** Class representing API Key */
case class ApiKey(
    keyId: Int,
    key: String,
    resourceId: Int,
    userId: Int,
    tierId: Int,
    dateIssued: Option[Timestamp],
    dateSuspended: Option[Timestamp]
)
