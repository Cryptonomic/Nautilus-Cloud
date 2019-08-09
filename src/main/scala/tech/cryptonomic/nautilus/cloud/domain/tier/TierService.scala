package tech.cryptonomic.nautilus.cloud.domain.tier

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.NotAllowedConfigurationOverride
import tech.cryptonomic.nautilus.cloud.domain.tools.ClockTool.ExtendedClock

import scala.language.higherKinds

/** Tier service implementation */
class TierService[F[_]: Monad](tierRepository: TierRepository[F], clock: Clock[F]) {

  /** Create tier */
  def createTier(name: TierName, createTier: CreateTier): F[Either[Throwable, Tier]] =
    for {
      now <- clock.currentInstant
      tier <- tierRepository.create(name, createTier.toConfiguration(now))
    } yield tier

  /** Update tier */
  def updateTier(name: TierName, updateTier: UpdateTier): F[Either[Throwable, Unit]] = ifYetToStart(updateTier) {
    tierRepository.addConfiguration(name, _)
  }

  /** Returns tier with given name */
  def getTier(name: TierName): F[Option[Tier]] =
    tierRepository.get(name)

  private def ifYetToStart(
      tier: UpdateTier
  )(f: TierConfiguration => F[Either[Throwable, Unit]]): F[Either[Throwable, Unit]] =
    for {
      now <- clock.currentInstant
      isValid = tier.startDate.exists(_ isAfter now)
      tierConfiguration = tier.toConfiguration(now)
      tier <- if (isValid)
        f(tierConfiguration)
      else
        (NotAllowedConfigurationOverride(s"Given time ${tierConfiguration.startDate} is from the past. Current time: $now"): Throwable)
          .asLeft[Unit]
          .pure[F]
    } yield tier
}
