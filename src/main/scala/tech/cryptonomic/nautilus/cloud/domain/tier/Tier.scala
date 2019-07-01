package tech.cryptonomic.nautilus.cloud.domain.tier

import java.time.Instant

case class Tier(
    name: TierName,
    configurations: List[TierConfiguration]
)

case class TierConfiguration(
    description: String,
    monthlyHits: Int,
    dailyHits: Int,
    maxResultSetSize: Int,
    endDate: Option[Instant] = None
)

case class TierName(tier: String, subTier: String) {
  override def toString: String = s"${tier}_$subTier"
}

object TierName {
  def apply(name: String): TierName =
    name.split('_') match {
      case Array(tier, subTier) => TierName(tier, subTier)
    }
}
