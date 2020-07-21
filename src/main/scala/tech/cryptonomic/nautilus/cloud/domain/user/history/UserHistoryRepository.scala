package tech.cryptonomic.nautilus.cloud.domain.user.history

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import scala.language.higherKinds

/** Interface for handling user history entries */
trait UserHistoryRepository[F[_]] {

  /** Inserts action to the user history repository */
  def insertUserHistoryEntry(userHistory: UserAction): F[Unit]

  /** Returns actions from the repository for the given user */
  def getUserHistory(userId: UserId): F[List[UserAction]]

}
