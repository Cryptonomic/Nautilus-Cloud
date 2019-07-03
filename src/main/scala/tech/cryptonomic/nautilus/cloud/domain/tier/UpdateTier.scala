package tech.cryptonomic.nautilus.cloud.domain.tier

import java.time.Instant

case class UpdateTier(
    description: String,
    monthlyHits: Int,
    dailyHits: Int,
    maxResultSetSize: Int,
    startDate: Instant
) {
  lazy val asConfiguration = TierConfiguration(description, monthlyHits, dailyHits, maxResultSetSize, startDate)
}
