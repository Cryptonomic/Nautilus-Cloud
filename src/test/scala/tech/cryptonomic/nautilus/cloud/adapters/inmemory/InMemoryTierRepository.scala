package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierName, TierRepository}

import scala.language.higherKinds

class InMemoryTierRepository[F[_]: Monad] extends TierRepository[F] {

  /** list of all tiers
    *
    * in order to be consistent with a real database we adjust reads and writes to keep indexing starting from 1 not
    * from 0
    */
  private var tiers: List[Tier] = List.empty

  /** Creates tier */
  override def create(name: TierName, createTier: CreateTier): F[Either[Throwable, Tier]] = this.synchronized {
    get(name).map {
      case Some(_) => Left(new RuntimeException)
      case None =>
        val tier = createTier.toTier(name)
        tiers = tiers :+ tier
        tier.asRight[Throwable]
    }
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