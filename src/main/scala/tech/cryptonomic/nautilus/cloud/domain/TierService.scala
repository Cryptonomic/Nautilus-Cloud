package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.NotAllowedConfigurationOverride
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{requiredRole, Permission}
import cats.Applicative
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{requiredRole, Permission}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.{
  CreateTier,
  Tier,
  TierConfiguration,
  TierName,
  TierRepository,
  UpdateTier
}
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.tools.ClockTool.ExtendedClock

import scala.concurrent.duration.MILLISECONDS
import scala.language.higherKinds

/** Tier service implementation */
class TierService[F[_]: Monad](tierRepository: TierRepository[F], clock: Clock[F]) {

  /** Create tier */
  def createTier(name: TierName, createTier: CreateTier)(
      implicit session: Session
  ): F[Permission[Either[Throwable, Tier]]] = requiredRole(Administrator) {
    for {
      now <- clock.currentInstant
      tier <- tierRepository.create(name, createTier.toConfiguration(now))
    } yield tier
  }

  /** Update tier */
  def updateTier(name: TierName, updateTier: UpdateTier)(
      implicit session: Session
  ): F[Permission[Either[Throwable, Unit]]] = requiredRole(Administrator) {
    ifDateIsNotFromThePast(updateTier) {
      tierRepository.addConfiguration(name, _)
    }
  }

  private def ifDateIsNotFromThePast(
      tier: UpdateTier
  )(f: TierConfiguration => F[Either[Throwable, Unit]]): F[Either[Throwable, Unit]] =
    for {
      now <- clock.currentInstant
      isValid = tier.startDate.exists(_ isAfter now)
      tierConfiguration = tier.toConfiguration(now)
      tier <- if (isValid)
        f(tierConfiguration)
      else
        (NotAllowedConfigurationOverride(s"Given time ${tier.startDate} is from the past. Current time: $now"): Throwable)
          .asLeft[Unit]
          .pure[F]
    } yield tier

  /** Returns tier with given name */
  def getTier(name: TierName)(implicit session: Session): F[Permission[Option[Tier]]] = requiredRole(Administrator) {
    tierRepository.get(name)
  }
}
