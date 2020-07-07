package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0

case class UserHistory(userId: UserId, performedBy: Option[UserId], time: Instant, ip: Option[String], action: String)

trait UserHistoryQueries {
  def insertUserHistory(userHistory: UserHistory): Update0 =
    sql"INSERT INTO user_history (userid, performed_by, time, ip, action) VALUES (${userHistory.userId}, ${userHistory.performedBy}, ${userHistory.time}, ${userHistory.ip}, ${userHistory.action})".update

  def selectUserHistory(userId: UserId): Query0[UserHistory] =
    sql"SELECT userid, performed_by, time, ip, action FROM user_history WHERE userid = $userId".query[UserHistory]
}
