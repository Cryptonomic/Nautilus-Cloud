package tech.cryptonomic.nautilus.cloud.domain

import cats.Applicative
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, requiredRole}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier._
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator

import scala.language.higherKinds

/** Tier service implementation */
class TierApplication[F[_]: Applicative](tierService: TierService[F]) {

  /** Create tier */
  def createTier(name: TierName, createTier: CreateTier)(
      implicit session: Session
  ): F[Permission[Either[Throwable, Tier]]] = requiredRole(Administrator) {
    tierService.createTier(name, createTier)
  }

  /** Update tier */
  def updateTier(name: TierName, updateTier: UpdateTier)(
      implicit session: Session
  ): F[Permission[Either[Throwable, Unit]]] = requiredRole(Administrator) {
    tierService.updateTier(name, updateTier)
  }

  /** Returns tier with given name */
  def getTier(name: TierName)(implicit session: Session): F[Permission[Option[Tier]]] = requiredRole(Administrator) {
    tierService.getTier(name)
  }
}
