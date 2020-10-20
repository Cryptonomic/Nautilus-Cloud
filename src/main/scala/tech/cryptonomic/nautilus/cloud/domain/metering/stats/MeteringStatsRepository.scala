package tech.cryptonomic.nautilus.cloud.domain.metering.stats

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

/** Interface for MeteringStats repository operations */
trait MeteringStatsRepository[F[_]] {

  /** Inserts metering stats to the DB */
  def insertStats(stats: List[AggregatedMeteringStats]): F[Unit]

  /** Fetches last stats for the given users */
  def getLastStats(list: List[UserId]): F[List[AggregatedMeteringStats]]

  /** Fetches stats for the given user */
  def getStatsPerUser(userId: UserId, from: Option[Instant] = None): F[List[AggregatedMeteringStats]]

}
