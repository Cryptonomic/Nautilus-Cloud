package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import endpoints.akkahttp.server
import tech.cryptonomic.nautilus.cloud.adapters.endpoints.{EndpointStatusSyntax, TierEndpoints}
import tech.cryptonomic.nautilus.cloud.application.TierApplication
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session

/** Tier routes implementation */
class TierRoutes(tierService: TierApplication[IO])
    extends TierEndpoints
    with server.Endpoints
    with EndpointStatusSyntax
    with StrictLogging {

  /** Tier create route implementation */
  def createTierRoute(implicit session: Session): Route =
    createTier.implementedByAsync {
      case (name, tier) => tierService.createTier(name, tier).unsafeToFuture()
    }

  /** Tier update route implementation */
  def updateTierRoute(implicit session: Session): Route =
    updateTier.implementedByAsync {
      case (name, tier) => tierService.updateTier(name, tier).unsafeToFuture()
    }

  /** Tier get route implementation */
  def getTierRoute(implicit session: Session): Route =
    getTier.implementedByAsync(tier => tierService.getTier(tier).unsafeToFuture())
}
