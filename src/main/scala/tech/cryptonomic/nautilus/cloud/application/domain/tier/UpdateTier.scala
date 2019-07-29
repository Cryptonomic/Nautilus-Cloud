package tech.cryptonomic.nautilus.cloud.application.domain.tier

import java.time.Instant

case class UpdateTier(
    description: String,
    usage: Usage,
    maxResultSetSize: Int,
    startDate: Option[Instant] = None
) {
  def toConfiguration(now: Instant) =
    TierConfiguration(description, usage, maxResultSetSize, startDate.getOrElse(now))
}
