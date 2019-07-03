package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect._

import scala.concurrent.duration.MILLISECONDS
import cats.Monad
import cats.data.EitherT
import cats.effect.Bracket
import cats.implicits._
import doobie.enum.SqlState
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierName, TierRepository, UpdateTier}

import scala.language.higherKinds

/** Trait representing User repo queries */
class DoobieTierRepository[F[_]: Monad](transactor: Transactor[F])(
    implicit bracket: Bracket[F, Throwable],
    clock: Clock[F]
) extends TierRepository[F]
    with TierQueries {

  val UNIQUE_VIOLATION = SqlState("23505")

  /** Creates tier */
  override def create(name: TierName, createTier: CreateTier): F[Either[Throwable, Tier]] = {
    def inserts(now: Instant) =
      for {
        _ <- EitherT(createTierQuery(name).run.attemptSomeSqlState {
          case UNIQUE_VIOLATION => DoobieUniqueUserViolationException("UNIQUE_VIOLATION"): Throwable
        })
        _ <- EitherT(createTierConfigurationQuery(name, createTier.toConfiguration(now)).run.attemptSomeSqlState {
          case UNIQUE_VIOLATION => DoobieUniqueUserViolationException("UNIQUE_VIOLATION"): Throwable
        })
      } yield ()

    val result = for {
      now <- EitherT.right(clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)): EitherT[F, Throwable, Instant]
      tier <- EitherT.rightT(createTier.toTier(name, now)): EitherT[F, Throwable, Tier]
      _ <- inserts(now).transact(transactor)
    } yield tier

    result.value
  }

  /** Updates tier */
  override def update(name: TierName, updateTier: UpdateTier): F[Either[Throwable, Unit]] = {
    val result = createTierConfigurationQuery(name, updateTier.asConfiguration).run.attemptSomeSqlState {
      case UNIQUE_VIOLATION => DoobieUniqueUserViolationException("UNIQUE_VIOLATION"): Throwable
    }.transact(transactor)

    EitherT(result).map(_ => ()).value
  }

  /** Returns tier */
  override def get(name: TierName): F[Option[Tier]] = {
    import tech.cryptonomic.nautilus.cloud.adapters.doobie.TierQueries._
    getTiersConfigurationQuery(name).to[List].transact(transactor).map(_.toTier)
  }
}

final case class DoobieUniqueTierViolationException(message: String) extends Throwable(message)
