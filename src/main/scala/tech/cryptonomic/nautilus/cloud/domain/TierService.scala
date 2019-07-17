package tech.cryptonomic.nautilus.cloud.domain

import cats.Applicative
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, requiredRole}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierName, TierRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator

import scala.language.higherKinds

/** Tier service implementation */
class TierService[F[_]: Applicative](tierRepository: TierRepository[F]) {

  /** Updated tier */
  def createTier(name: TierName, tier: CreateTier)(implicit session: Session): F[Permission[Either[Throwable, Tier]]] =
    requiredRole(Administrator) {
      tierRepository.create(name, tier)
    }

  /** Returns tier with given name */
  def getTier(name: TierName)(implicit session: Session): F[Permission[Option[Tier]]] = requiredRole(Administrator) {
    tierRepository.get(name)
  }
}
