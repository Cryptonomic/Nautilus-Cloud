package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._
import scala.concurrent.duration.MILLISECONDS
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierName, TierRepository, UpdateTier}

import scala.language.higherKinds

class InMemoryTierRepository[F[_]: Monad](clock: Clock[F]) extends TierRepository[F] {

  /** list of all tiers
    *
    * in order to be consistent with a real database we adjust reads and writes to keep indexing starting from 1 not
    * from 0
    */
  private var tiers: List[Tier] = List.empty

  /** Creates tier */
  override def create(name: TierName, createTier: CreateTier): F[Either[Throwable, Tier]] = this.synchronized {
    for {
      tier <- get(name)
      now <- clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)
    } yield
      tier match {
        case Some(_) => Left(new RuntimeException)
        case None =>
          val tier = createTier.toTier(name, now)
          tiers = tiers :+ tier
          tier.asRight[Throwable]
      }
  }

  /** Updates tier */
  override def update(name: TierName, tier: UpdateTier): F[Either[Throwable, Unit]] = this.synchronized {
    tiers = tiers.collect {
      case t @ Tier(`name`, _) => t.copy(configurations = t.configurations :+ tier.asConfiguration)
      case it => it
    }

    ().asRight[Throwable].pure[F]
  }

  /** Returns tier */
  override def get(name: TierName): F[Option[Tier]] = this.synchronized {
    tiers.find(_.name == name).pure[F]
  }

  /** Clears repository */
  def clear(): Unit = this.synchronized {
    tiers = List.empty
  }
}
