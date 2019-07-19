package tech.cryptonomic.nautilus.cloud.domain.tier

import scala.language.higherKinds

/** Trait representing Tier repo queries */
trait TierRepository[F[_]] {

  /** Creates tier */
  def create(name: TierName, initialConfiguration: TierConfiguration): F[Either[Throwable, Tier]]

  /** Updates tier */
  def addConfiguration(name: TierName, tierConfiguration: TierConfiguration): F[Either[Throwable, Unit]]

  /** Returns tier by TierName*/
  def get(name: TierName): F[Option[Tier]]

  /** Returns tier by ID */
  def get(tierId: Int): F[Option[Tier]]

}
