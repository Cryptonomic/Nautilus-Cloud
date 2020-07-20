package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Applicative
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.history.{UserAction, UserHistoryRepository}

import scala.language.higherKinds

/** In memory implementation of UserHistoryRepository */
class InMemoryUserHistoryRepository[F[_]: Applicative] extends UserHistoryRepository[F] {
  private var userActions = List.empty[UserAction]

  /** Inserts action to the user history repository */
  override def insertUserHistoryEntry(userAction: UserAction): F[Unit] =
    this.synchronized {
      userActions = userAction :: userActions
    }.pure[F]

  /** Returns actions from the repository for the given user */
  override def getUserHistory(userId: UserId): F[List[UserAction]] =
    this.synchronized {
      userActions.filter(_.userId == userId)
    }.pure[F]
}
