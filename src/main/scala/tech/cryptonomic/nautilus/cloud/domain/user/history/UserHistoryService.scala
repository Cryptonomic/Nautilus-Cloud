package tech.cryptonomic.nautilus.cloud.domain.user.history

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

import scala.language.higherKinds

class UserHistoryService[F[_]](userHistoryRepository: UserHistoryRepository[F]) {

  def insertUserAction(userAction: UserAction): F[Unit] = userHistoryRepository.insertUserHistoryEntry(userAction)

  def getHistoryForUser(userId: UserId): F[List[UserAction]] = userHistoryRepository.getUserHistory(userId)

}
