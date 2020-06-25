package tech.cryptonomic.nautilus.cloud.adapters.doobie

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.AggregatedMeteringStats
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/** Queries for metering_stats table */
trait MeteringStatsQueries {

  /** Gathers last recorded interval per user ID */
  def lastRecordedIntervalPerUser: Query0[AggregatedMeteringStats] =
    sql"SELECT DISTINCT ON(userid) userid, service, hits, period_start, period_end FROM (SELECT userid, service, hits, period_start, period_end FROM metering_statistics ORDER BY period_end DESC) AS foo"
      .query[AggregatedMeteringStats]

  /** Inserts metering stats */
  def insertMeteringStats(stats: AggregatedMeteringStats): Update0 =
    sql"INSERT INTO metering_statistics (userid, service, hits, period_start, period_end) values (${stats.userId}, ${stats.service}, ${stats.hits}, ${stats.periodStart}, ${stats.periodEnd})".update

  /** Fetches metering stats for given user */
  def meteringStatsPerUser(userId: UserId): Query0[AggregatedMeteringStats] =
    sql"SELECT userid, service, hits, period_start, period_end FROM metering_statistics WHERE userid = $userId ORDER BY period_end DESC"
      .query[AggregatedMeteringStats]

}
