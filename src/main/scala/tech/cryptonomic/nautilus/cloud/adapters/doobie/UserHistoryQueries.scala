package tech.cryptonomic.nautilus.cloud.adapters.doobie


import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.user.UserAction


trait UserHistoryQueries {
  def insertUserHistory(userAction: UserAction): Update0 =
    sql"INSERT INTO user_history (userid, performed_by, time, ip, action) VALUES (${userAction.userId}, ${userAction.performedBy}, ${userAction.time}, ${userAction.ip}, ${userAction.action})".update

  def selectUserHistory(userId: UserId): Query0[UserAction] =
    sql"SELECT userid, performed_by, time, ip, action FROM user_history WHERE userid = $userId".query[UserAction]
}
