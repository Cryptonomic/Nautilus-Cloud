package tech.cryptonomic.nautilus.cloud.application.domain.apiKey

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.ApiKey.KeyId
import io.scalaland.chimney.dsl._

/** Model for creating API key */
case class CreateApiKey(
    key: String,
    environment: Environment,
    userId: Int,
    dateIssued: Instant,
    dateSuspended: Option[Instant]
) {

  /** Transforms CreateApiKey into ApiKey with given KeyId */
  def toApiKey(keyId: KeyId): ApiKey =
    this.into[ApiKey].withFieldConst(_.keyId, keyId).withFieldConst(_.dateIssued, Some(dateIssued)).transform
}
