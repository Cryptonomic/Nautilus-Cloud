package tech.cryptonomic.nautilus.cloud.domain.tier

import scala.language.higherKinds

/** Trait representing Tier repo queries */
trait TierRepository[F[_]] {

  /** Creates tier */
  def create(name: TierName, tier: CreateTier): F[Either[Throwable, Tier]]

  /** Returns tier */
  def get(name: TierName): F[Option[Tier]]
}
