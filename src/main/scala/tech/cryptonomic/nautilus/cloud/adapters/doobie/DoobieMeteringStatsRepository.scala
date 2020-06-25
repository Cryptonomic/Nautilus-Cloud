package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.effect.Bracket
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.{AggregatedMeteringStats, MeteringStatsRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

class DoobieMeteringStatsRepository[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable])
    extends MeteringStatsRepository[F]
    with MeteringStatsQueries {

  /** Inserts metering stats to the DB */
  override def insertStats(stats: List[AggregatedMeteringStats]): F[Unit] =
    stats.map(stat => insertMeteringStats(stat).run.void.transact(transactor)).sequence.map(_ => ())

  /** Fetches last stats for the given users */
  override def getLastStats(list: List[UserId]): F[List[AggregatedMeteringStats]] =
    lastRecordedIntervalPerUser.to[List].transact(transactor)

  /** Fetches stats for the given user */
  override def getStatsPerUser(userId: UserId): F[List[AggregatedMeteringStats]] =
    meteringStatsPerUser(userId).to[List].transact(transactor)
}
