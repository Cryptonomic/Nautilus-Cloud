package tech.cryptonomic.nautilus.cloud.domain.tier

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.{DailyMonthlyHits, TierId}

case class Tier(
    tierId: TierId,
    name: TierName,
    configurations: List[TierConfiguration]
) {

  /** Returns valid daily/monthly hit rates */
  def findValidDailyMonthlyHits(instant: Instant): DailyMonthlyHits =
    configurations
      .find(conf => conf.startDate.isAfter(instant))
      .map(conf => conf.dailyHits -> conf.monthlyHits)
      .getOrElse((0, 0)) // every tier should have valid configuration, but in case it doesn't we return (0, 0)

}

object Tier {
  type TierId = Int
  type DailyMonthlyHits = (Int, Int)
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
