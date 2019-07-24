package tech.cryptonomic.nautilus.cloud.domain.tier

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId

/* case class for create tier */
case class CreateTier(
    description: String,
    monthlyHits: Int,
    dailyHits: Int,
    maxResultSetSize: Int
) {
  def toConfiguration(instant: Instant) =
    TierConfiguration(description, monthlyHits, dailyHits, maxResultSetSize, instant)
  def toTier(tierId: TierId, name: TierName, now: Instant) = Tier(tierId, name, List(toConfiguration(now)))
}
