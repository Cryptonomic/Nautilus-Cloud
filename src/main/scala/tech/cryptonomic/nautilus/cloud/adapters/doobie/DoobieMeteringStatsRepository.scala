package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.effect.Bracket
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.{AggregatedMeteringStats, MeteringStatsRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** Doobie implementation for the MeteringStatsRepository with it's functionalities */
class DoobieMeteringStatsRepository[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable])
    extends MeteringStatsRepository[F]
    with MeteringStatsQueries {

  /** Inserts metering stats to the DB */
  override def insertStats(stats: List[AggregatedMeteringStats]): F[Unit] =
    stats.traverse(insertMeteringStats(_).run).void.transact(transactor)

  /** Fetches latest gathered stats for given users */
  override def getLastStats(list: List[UserId]): F[List[AggregatedMeteringStats]] =
    lastRecordedIntervalPerUser.to[List].transact(transactor)

  /** Fetches all stats for the given user */
  override def getStatsPerUser(userId: UserId): F[List[AggregatedMeteringStats]] =
    meteringStatsPerUser(userId).to[List].transact(transactor)
}
