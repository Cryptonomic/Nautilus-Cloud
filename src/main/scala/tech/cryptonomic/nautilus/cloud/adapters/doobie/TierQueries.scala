package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierConfiguration, TierName}

/** Trait containing User related queries */
trait TierQueries {

  /** Creates tier */
  def createTierQuery(name: TierName): Update0 =
    sql"""INSERT INTO tiers (tier, subtier) VALUES (${name.tier},${name.subTier})""".update

  /** Creates tier configuration */
  def createTierConfigurationQuery(name: TierName, tier: CreateTier): Update0 =
    sql"""INSERT into tiers_configuration (tier, subtier, description, monthlyhits, dailyhits, maxresultsetsize)
          VALUES (${name.tier}, ${name.subTier}, ${tier.description}, ${tier.monthlyHits}, ${tier.dailyHits},
                  ${tier.maxResultSetSize})""".update

  /** Returns tier */
  def getTiersConfigurationQuery(tierName: TierName): Query0[TierConfigurationDto] =
    sql"""SELECT tier, subtier, description, monthlyhits, dailyhits, maxResultSetSize, enddate FROM tiers_configuration
          WHERE tier = ${tierName.tier} and subtier = ${tierName.subTier}""".query[TierConfigurationDto]

  /** Returns tier */
  def getTiersConfigurationQuery(tierId: Int): Query0[TierConfigurationDto] =
    sql"""SELECT tiers_configuration.tier, tiers_configuration.subtier, description, monthlyhits, dailyhits, maxResultSetSize, enddate
          FROM tiers_configuration JOIN tiers ON tiers.tier = tiers_configuration.tier AND tiers.subtier = tiers_configuration.subtier
          WHERE tierid = $tierId""".query[TierConfigurationDto]
}

object TierQueries {

  /* extension method which converts List[TierDto] to a Tier */
  implicit class ExtendedTierDtoList(val tiers: List[TierConfigurationDto]) extends AnyVal {
    def toTier: Option[Tier] =
      tiers.headOption
        .map(head => Tier(head.asTierName, tiers.map(_.asTierConfiguration)))
  }
}

/* Dto for TierConfiguration */
case class TierConfigurationDto(
    tier: String,
    subtier: String,
    description: String,
    monthlyhits: Int,
    dailyhits: Int,
    maxResultSetSize: Int,
    enddate: Option[Instant]
) {
  lazy val asTierName = TierName(tier, subtier)
  lazy val asTierConfiguration = TierConfiguration(description, monthlyhits, dailyhits, maxResultSetSize, enddate)
}
