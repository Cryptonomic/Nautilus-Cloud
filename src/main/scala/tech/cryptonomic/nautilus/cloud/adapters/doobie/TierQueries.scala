package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierConfiguration, TierName, Usage}

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
          VALUES (${name.tier}, ${name.subTier}, ${configuration.description}, ${configuration.usage.monthly},
                  ${configuration.usage.daily}, ${configuration.maxResultSetSize}, ${configuration.startDate})""".update

  /** Returns tier */
  def getTiersConfigurationQuery(tierName: TierName): Query0[TierConfigurationDto] =
    sql"""SELECT tiers.tierid, tiers_configuration.tier, tiers_configuration.subtier, description, monthlyhits, dailyhits, maxResultSetSize, startdate
          FROM tiers_configuration JOIN tiers ON tiers.tier = tiers_configuration.tier AND tiers.subtier = tiers_configuration.subtier
          WHERE tiers_configuration.tier = ${tierName.tier} and tiers_configuration.subtier = ${tierName.subTier}"""
      .query[TierConfigurationDto]

  /** Returns tier */
  def getTiersConfigurationQuery(tierId: TierId): Query0[TierConfigurationDto] =
    sql"""SELECT tiers.tierid, tiers_configuration.tier, tiers_configuration.subtier, description, monthlyhits, dailyhits, maxResultSetSize, startdate
          FROM tiers_configuration JOIN tiers ON tiers.tier = tiers_configuration.tier AND tiers.subtier = tiers_configuration.subtier
          WHERE tierid = $tierId""".query[TierConfigurationDto]
}

object TierQueries {

  /* extension method which converts List[TierDto] to a Tier */
  implicit class ExtendedTierDtoList(val tiers: List[TierConfigurationDto]) extends AnyVal {
    def toTier: Option[Tier] =
      tiers.headOption
        .map(head => Tier(head.tierid, head.asTierName, tiers.map(_.asTierConfiguration)))
  }
}

/* Dto for TierConfiguration */
case class TierConfigurationDto(
    tierid: TierId,
    tier: String,
    subtier: String,
    description: String,
    monthlyhits: Int,
    dailyhits: Int,
    maxResultSetSize: Int,
    startdate: Instant
) {
  lazy val asTierName = TierName(tier, subtier)
  lazy val asTierConfiguration =
    TierConfiguration(description, Usage(dailyhits, monthlyhits), maxResultSetSize, startdate)
}
