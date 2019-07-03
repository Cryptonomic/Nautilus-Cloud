package tech.cryptonomic.nautilus.cloud.domain.tier

import java.time.Instant

case class CreateTier(
    description: String,
    monthlyHits: Int,
    dailyHits: Int,
    maxResultSetSize: Int
) {
  def toConfiguration(instant: Instant) =
    TierConfiguration(description, monthlyHits, dailyHits, maxResultSetSize, instant)
  def toTier(name: TierName, now: Instant) = Tier(name, List(toConfiguration(now)))
}
