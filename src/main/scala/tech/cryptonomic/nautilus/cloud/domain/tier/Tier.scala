package tech.cryptonomic.nautilus.cloud.domain.tier

import java.time.Instant

case class Tier(
    name: TierName,
    configurations: List[TierConfiguration]
)

object Tier {
  val defaultTierId = 1
}

case class TierConfiguration(
    description: String,
    monthlyHits: Int,
    dailyHits: Int,
    maxResultSetSize: Int,
    startDate: Instant
)

case class TierName(tier: String, subTier: String) {
  override def toString: String = s"${tier}_$subTier"
}

object TierName {
  /* apply method for constructing tier name base on string with "_" as delimiter between tier and subTier */
  def apply(name: String): TierName =
    name.split('_') match {
      case Array(tier, subTier) => TierName(tier, subTier)
    }
}
