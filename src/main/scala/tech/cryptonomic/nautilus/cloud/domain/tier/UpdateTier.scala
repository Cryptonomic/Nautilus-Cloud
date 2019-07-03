package tech.cryptonomic.nautilus.cloud.domain.tier

import java.time.Instant

case class UpdateTier(
    description: String,
    monthlyHits: Int,
    dailyHits: Int,
    maxResultSetSize: Int,
    startDate: Option[Instant] = None
) {
  def toConfiguration(now: Instant) =
    TierConfiguration(description, monthlyHits, dailyHits, maxResultSetSize, startDate.getOrElse(now))
}
