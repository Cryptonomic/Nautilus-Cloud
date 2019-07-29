package tech.cryptonomic.nautilus.cloud.application.domain.tier

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.application.domain.tier.Tier.TierId

/* case class for create tier */
case class CreateTier(
    description: String,
    usage: Usage,
    maxResultSetSize: Int
) {
  def toConfiguration(instant: Instant) =
    TierConfiguration(description, usage, maxResultSetSize, instant)
  def toTier(tierId: TierId, name: TierName, now: Instant) = Tier(tierId, name, List(toConfiguration(now)))
}
