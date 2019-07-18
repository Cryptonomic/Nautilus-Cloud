package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.Monad
import cats.data.EitherT
import cats.effect.Bracket
import cats.implicits._
import doobie.enum.SqlState
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierName, TierRepository}

import scala.language.higherKinds

/** Trait representing User repo queries */
class DoobieTierRepository[F[_]: Monad](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable])
    extends TierRepository[F]
    with TierQueries {

  val UNIQUE_VIOLATION = SqlState("23505")

  /** Creates tier */
  override def create(name: TierName, createTier: CreateTier): F[Either[Throwable, Tier]] = {
    val inserts = for {
      _ <- EitherT(createTierQuery(name).run.attemptSomeSqlState {
        case UNIQUE_VIOLATION => DoobieUniqueTierViolationException("UNIQUE_VIOLATION"): Throwable
      })
      _ <- EitherT(createTierConfigurationQuery(name, createTier).run.attemptSomeSqlState {
        case UNIQUE_VIOLATION => DoobieUniqueTierViolationException("UNIQUE_VIOLATION"): Throwable
      })
    } yield createTier.toTier(name)

    inserts
      .transact(transactor)
      .value
  }

  /** Returns tier by Tier Name */
  override def get(name: TierName): F[Option[Tier]] = {
    import tech.cryptonomic.nautilus.cloud.adapters.doobie.TierQueries._
    getTiersConfigurationQuery(name).to[List].transact(transactor).map(_.toTier)
  }

  /** Returns tier by ID */
  override def get(tierId: Int): F[Option[Tier]] = {
    import tech.cryptonomic.nautilus.cloud.adapters.doobie.TierQueries._
    getTiersConfigurationQuery(tierId).to[List].transact(transactor).map(_.toTier)
  }

  /** Creates default tier */
  override def createDefaultTier: F[Either[Throwable, Tier]] =
    create(TierName("shared", "free"), CreateTier("free tier", 1000, 100, 10))

}

final case class DoobieUniqueTierViolationException(message: String) extends Throwable(message)
