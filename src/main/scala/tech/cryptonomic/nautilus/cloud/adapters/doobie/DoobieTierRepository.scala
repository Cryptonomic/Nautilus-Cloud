package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import scala.concurrent.duration.MILLISECONDS
import cats.Monad
import cats.data.EitherT
import cats.effect.Bracket
import cats.implicits._
import doobie.enum.SqlState
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierConfiguration, TierName, TierRepository, UpdateTier}

import scala.language.higherKinds

/** Trait representing User repo queries */
class DoobieTierRepository[F[_]: Monad](transactor: Transactor[F])(
    implicit bracket: Bracket[F, Throwable]
) extends TierRepository[F]
    with TierQueries {

  val UNIQUE_VIOLATION = SqlState("23505")

  /** Creates tier */
  override def create(name: TierName, initialConfiguration: TierConfiguration): F[Either[Throwable, Tier]] = {
    val result = for {
      _ <- EitherT(createTierQuery(name).run.attemptSomeSqlState {
        case UNIQUE_VIOLATION => DoobieUniqueUserViolationException("UNIQUE_VIOLATION"): Throwable
      })
      _ <- EitherT(createTierConfigurationQuery(name, initialConfiguration).run.attemptSomeSqlState {
        case UNIQUE_VIOLATION => DoobieUniqueUserViolationException("UNIQUE_VIOLATION"): Throwable
      })
    } yield Tier(name, List(initialConfiguration))

    result.transact(transactor).value
  }

  /** Updates tier */
  override def addConfiguration(name: TierName, configuration: TierConfiguration): F[Either[Throwable, Unit]] = {
    val result = EitherT(createTierConfigurationQuery(name, configuration).run.attemptSomeSqlState {
      case UNIQUE_VIOLATION => DoobieUniqueUserViolationException("UNIQUE_VIOLATION"): Throwable
    }.transact(transactor))

    result.map(_ => ()).value
  }

  /** Returns tier */
  override def get(name: TierName): F[Option[Tier]] = {
    import tech.cryptonomic.nautilus.cloud.adapters.doobie.TierQueries._
    getTiersConfigurationQuery(name).to[List].transact(transactor).map(_.toTier)
  }
}

final case class DoobieUniqueTierViolationException(message: String) extends Throwable(message)
