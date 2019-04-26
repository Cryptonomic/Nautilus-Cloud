package tech.cryptonomic.cloud.nautilus.model

import java.sql.Timestamp

case class ApiKey(
    keyId: Long,
    key: String,
    resourceId: Long,
    userId: Long,
    tierId: Long,
    dateIssued: Option[Timestamp],
    dateSuspended: Option[Timestamp]
)
