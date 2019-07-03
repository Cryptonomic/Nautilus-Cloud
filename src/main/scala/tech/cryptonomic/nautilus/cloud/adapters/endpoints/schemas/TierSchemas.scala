package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import endpoints.algebra
import endpoints.algebra.Urls
import endpoints.generic.JsonSchemas
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierConfiguration, TierName, UpdateTier}

import scala.util.Try

/** Schemas used for User endpoints */
trait TierSchemas extends algebra.JsonSchemas with JsonSchemas with Urls with InstantSchema {

  /** Create tier schema */
  implicit lazy val createTierSchema: JsonSchema[CreateTier] = genericJsonSchema[CreateTier]

  /** Update tier schema */
  implicit lazy val updateTierSchema: JsonSchema[UpdateTier] = genericJsonSchema[UpdateTier]

  /** Tier name schema */
  implicit lazy val tierNameSchema: JsonSchema[TierName] = stringJsonSchema.xmap(TierName(_))(_.toString)

  /** Tier configuration schema */
  implicit lazy val tierConfigurationSchema: JsonSchema[TierConfiguration] = genericJsonSchema[TierConfiguration]

  /** Tier schema */
  implicit lazy val tierSchema: JsonSchema[Tier] = genericJsonSchema[Tier]

  /** Tier name segment */
  implicit lazy val tierNameSegment: Segment[TierName] =
    refineSegment(stringSegment)(it => Try(TierName(it)).toOption)(_.toString)
}
