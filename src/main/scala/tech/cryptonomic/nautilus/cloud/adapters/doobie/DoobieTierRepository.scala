package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.Monad
import cats.data.EitherT
import cats.effect.Bracket
import cats.implicits._
import doobie.enum.SqlState
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierConfiguration, TierName, TierRepository}

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
        case UNIQUE_VIOLATION => DoobieUniqueTierViolationException("UNIQUE_VIOLATION"): Throwable
      })
      _ <- EitherT(createTierConfigurationQuery(name, initialConfiguration).run.attemptSomeSqlState {
        case UNIQUE_VIOLATION => DoobieUniqueTierViolationException("UNIQUE_VIOLATION"): Throwable
      })
    } yield Tier(name, List(initialConfiguration))

    result.transact(transactor).value
  }

  /** Updates tier */
  override def addConfiguration(name: TierName, configuration: TierConfiguration): F[Either[Throwable, Unit]] = {
    val transaction = for {
      isValid <- validateTierConfigurationQuery(name, configuration).unique.map(_ == 0)
      result <- if (isValid)
        createTierConfigurationQuery(name, configuration).run.map(_ => ().asRight[Throwable])
      else
        (NotAllowedConfigurationOverride(""): Throwable).asLeft[Unit].pure[ConnectionIO]
    } yield result

    transaction.transact(transactor)
  }

  /** Returns tier */
  override def get(name: TierName): F[Option[Tier]] = {
    import tech.cryptonomic.nautilus.cloud.adapters.doobie.TierQueries._
    getTiersConfigurationQuery(name).to[List].transact(transactor).map(_.toTier)
  }
}

final case class DoobieUniqueTierViolationException(message: String) extends Throwable(message)

final case class NotAllowedConfigurationOverride(message: String) extends Throwable(message)
