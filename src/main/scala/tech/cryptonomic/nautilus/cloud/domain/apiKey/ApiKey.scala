package tech.cryptonomic.nautilus.cloud.domain.apiKey

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey.KeyId

/** Class representing API Key */
case class ApiKey(
    keyId: KeyId,
    key: String,
    resourceId: Int,
    userId: Int,
    tierId: Int,
    dateIssued: Option[Instant],
    dateSuspended: Option[Instant]
)

object ApiKey {
  type KeyId = Int
}
