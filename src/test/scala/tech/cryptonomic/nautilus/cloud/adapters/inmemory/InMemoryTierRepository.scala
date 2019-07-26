package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierConfiguration, TierName, TierRepository}

import scala.language.higherKinds

class InMemoryTierRepository[F[_]: Monad] extends TierRepository[F] {

  /** list of all tiers
    *
    * in order to be consistent with a real database we adjust reads and writes to keep indexing starting from 1 not
    * from 0
    */
  private var tiers: List[Tier] = List.empty

  /** Creates tier */
  override def create(name: TierName, initialConfiguration: TierConfiguration): F[Either[Throwable, Tier]] =
    this.synchronized {
      get(name).map {
        case Some(_) => Left(new RuntimeException)
        case None =>
          val tier = Tier(tiers.map(_.tierId).maximumOption.getOrElse(0) + 1, name, List(initialConfiguration))
          tiers = tiers :+ tier
          tier.asRight[Throwable]
      }
    }

  /** Updates tier */
  override def addConfiguration(name: TierName, tierConfiguration: TierConfiguration): F[Either[Throwable, Unit]] =
    this.synchronized {
      tiers = tiers.collect {
        case t @ Tier(_, `name`, _) => t.copy(configurations = t.configurations :+ tierConfiguration)
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

  /** Returns tier by ID */
  override def get(tierId: TierId): F[Option[Tier]] = this.synchronized {
    Option(tiers(tierId - 1)).pure[F]
  }

  /** Returns default Tier */
  override def getDefaultTier: F[Option[Tier]] = this.synchronized {
    get(TierName("shared", "free"))
  }
}
