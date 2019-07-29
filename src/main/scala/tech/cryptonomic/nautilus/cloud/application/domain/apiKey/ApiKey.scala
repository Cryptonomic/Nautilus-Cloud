package tech.cryptonomic.nautilus.cloud.application.domain.apiKey

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.ApiKey.KeyId

/** Class representing API Key */
case class ApiKey(
    keyId: KeyId,
    key: String,
    environment: Environment,
    userId: Int,
    dateIssued: Option[Instant],
    dateSuspended: Option[Instant]
)

object ApiKey {
  type KeyId = Int
}
