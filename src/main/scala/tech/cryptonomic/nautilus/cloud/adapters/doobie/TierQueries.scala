package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierConfiguration, TierName}

/** Trait containing User related queries */
trait TierQueries {

  /** Creates tier */
  def createTierQuery(name: TierName): Update0 =
    sql"""INSERT INTO tiers (tier, subtier) VALUES (${name.tier},${name.subTier})""".update

  def validateTierConfigurationQuery(name: TierName, configuration: TierConfiguration): Query0[Long] =
    sql"""SELECT count(*) FROM tiers_configuration WHERE
          tier = ${name.tier} AND
          subtier = ${name.subTier} AND
          startdate >= ${configuration.startDate}""".query

  /** Creates tier configuration */
  def createTierConfigurationQuery(name: TierName, configuration: TierConfiguration): Update0 =
    sql"""INSERT INTO tiers_configuration (tier, subtier, description, monthlyhits, dailyhits, maxresultsetsize, startdate)
          VALUES (${name.tier}, ${name.subTier}, ${configuration.description}, ${configuration.monthlyHits},
                  ${configuration.dailyHits}, ${configuration.maxResultSetSize}, ${configuration.startDate})""".update

  /** Returns tier */
  def getTiersConfigurationQuery(tierName: TierName): Query0[TierDto] =
    sql"""SELECT tier, subtier, description, monthlyhits, dailyhits, maxResultSetSize, startdate FROM tiers_configuration
          WHERE tier = ${tierName.tier} and subtier = ${tierName.subTier}""".query
}

object TierQueries {
  implicit class ExtendedTierDtoList(val tiers: List[TierDto]) extends AnyVal {
    def toTier: Option[Tier] =
      tiers.headOption
        .map(head => Tier(head.asTierName, tiers.map(_.asTierConfiguration)))
  }
}

case class TierDto(
    tier: String,
    subtier: String,
    description: String,
    monthlyhits: Int,
    dailyhits: Int,
    maxResultSetSize: Int,
    startdate: Instant
) {
  lazy val asTierName = TierName(tier, subtier)
  lazy val asTierConfiguration = TierConfiguration(description, monthlyhits, dailyhits, maxResultSetSize, startdate)
}
