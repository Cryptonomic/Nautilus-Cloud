package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.{AggregatedMeteringStats, MeteringStatsRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import scala.language.higherKinds

class InMemoryMeteringStatsRepository[F[_]: Monad] extends MeteringStatsRepository[F] {

  var statsRepository = List.empty[AggregatedMeteringStats]

  /** Inserts metering stats to the DB */
  override def insertStats(stats: List[AggregatedMeteringStats]): F[Unit] =
    this.synchronized {
      statsRepository = statsRepository ::: stats
              .filterNot(
                stat =>
                  statsRepository.exists(
                    rep =>
                      stat.userId == rep.userId && stat.periodStart == rep.periodStart && stat.periodEnd == rep.periodEnd
                  )
              )
    }.pure[F]

  /** Fetches last stats for the given users */
  override def getLastStats(list: List[UserId]): F[List[AggregatedMeteringStats]] =
    this.synchronized {
      statsRepository.groupBy(_.userId).values.map(_.sortBy(_.periodEnd).reverse.head).toList
    }.pure[F]

  /** Fetches stats for the given user */
  override def getStatsPerUser(userId: UserId): F[List[AggregatedMeteringStats]] =
    this.synchronized {
      println(statsRepository)
      statsRepository.filter(_.userId == userId)
    }.pure[F]
}
