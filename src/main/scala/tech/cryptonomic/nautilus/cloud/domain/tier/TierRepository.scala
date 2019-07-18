package tech.cryptonomic.nautilus.cloud.domain.tier

import scala.language.higherKinds

/** Trait representing Tier repo queries */
trait TierRepository[F[_]] {

  /** Creates tier */
  def create(name: TierName, tier: CreateTier): F[Either[Throwable, Tier]]

  /** Returns tier by TierName*/
  def get(name: TierName): F[Option[Tier]]

  /** Returns tier by ID */
  def get(tierId: Int): F[Option[Tier]]

  /** Creates default tier */
  def createDefaultTier: F[Either[Throwable, Tier]]
}
