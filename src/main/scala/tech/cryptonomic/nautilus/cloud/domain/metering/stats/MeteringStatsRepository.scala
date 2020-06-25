package tech.cryptonomic.nautilus.cloud.domain.metering.stats

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

trait MeteringStatsRepository[F[_]] {

  /** Inserts metering stats to the DB */
  def insertStats(stats: List[AggregatedMeteringStats]): F[Unit]

  /** Fetches last stats for the given users */
  def getLastStats(list: List[UserId]): F[List[AggregatedMeteringStats]]

  /** Fetches stats for the given user */
  def getStatsPerUser(userId: UserId): F[List[AggregatedMeteringStats]]

}
