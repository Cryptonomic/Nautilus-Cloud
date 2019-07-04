package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.NotAllowedConfigurationOverride
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{requiredRole, Permission}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierName, TierRepository, UpdateTier}
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator

import scala.concurrent.duration.MILLISECONDS
import scala.language.higherKinds

/** Tier service implementation */
class TierService[F[_]: Monad](tierRepository: TierRepository[F], clock: Clock[F]) {

  /** Create tier */
  def createTier(name: TierName, createTier: CreateTier)(
      implicit session: Session
  ): F[Permission[Either[Throwable, Tier]]] =
    requiredRole(Administrator) {
      for {
        now <- clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)
        tier <- tierRepository.create(name, createTier.toConfiguration(now))
      } yield tier
    }

  /** Update tier */
  def updateTier(name: TierName, updateTier: UpdateTier)(
      implicit session: Session
  ): F[Permission[Either[Throwable, Unit]]] =
    requiredRole(Administrator) {
      for {
        now <- clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)
        isValid = updateTier.startDate.exists(_ isAfter now)
        tier <- if (isValid) tierRepository.addConfiguration(name, updateTier.toConfiguration(now))
        else (NotAllowedConfigurationOverride(""): Throwable).asLeft[Unit].pure[F]
      } yield tier
    }

  /** Returns tier with given name */
  def getTier(name: TierName)(implicit session: Session): F[Permission[Option[Tier]]] = requiredRole(Administrator) {
    tierRepository.get(name)
  }
}
