package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas.TierSchemas
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierName, UpdateTier}

/** Tier endpoints */
trait TierEndpoints
    extends algebra.Endpoints
    with algebra.JsonSchemaEntities
    with TierSchemas
    with EndpointsStatusDefinitions {

  /** Tier create endpoint definition */
  def createTier: Endpoint[(TierName, CreateTier), Permission[Either[Throwable, Tier]]] =
    endpoint(
      request = put(url = path / "tiers" / segment[TierName]("tierName"), jsonRequest[CreateTier]()),
      response = jsonResponse[Tier]().withCreatedStatus().orBadRequest().orForbidden(),
      tags = List("Tier")
    )

  /** Tier update endpoint definition */
  def updateTier: Endpoint[(TierName, UpdateTier), Permission[Either[Throwable, Unit]]] =
    endpoint(
      request = post(url = path / "tiers" / segment[TierName]("tierName") / "configurations", jsonRequest[UpdateTier]()),
      response = emptyResponse().withCreatedStatus().orBadRequest().orForbidden(),
      tags = List("Tier")
    )

  /** Tier endpoint definition */
  def getTier: Endpoint[TierName, Permission[Option[Tier]]] =
    endpoint(
      request = get(url = path / "tiers" / segment[TierName]("tierName")),
      response = jsonResponse[Tier]().orNotFound().orForbidden(),
      tags = List("Tier")
    )
}
