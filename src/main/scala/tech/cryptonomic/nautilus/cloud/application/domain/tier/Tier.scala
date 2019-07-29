package tech.cryptonomic.nautilus.cloud.application.domain.tier

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.application.domain.tier.Tier.TierId

case class Tier(
    tierId: TierId,
    name: TierName,
    configurations: List[TierConfiguration]
) {

  /** Returns valid daily/monthly hit rates */
  def getCurrentUsage(now: Instant): Usage =
    configurations
      .find(conf => conf.startDate.isAfter(now))
      .map(_.usage)
      .getOrElse(Usage.default)
}

object Tier {
  type TierId = Int
}

case class TierConfiguration(
    description: String,
    usage: Usage,
    maxResultSetSize: Int,
    startDate: Instant
)

case class Usage(daily: Int, monthly: Int)

object Usage {
  lazy val default = Usage(0, 0)
}

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
