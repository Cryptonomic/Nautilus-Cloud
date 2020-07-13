package tech.cryptonomic.nautilus.cloud.adapters.doobie


import tech.cryptonomic.nautilus.cloud.domain.user.{UserAction, UserHistoryRepository}
import scala.language.higherKinds
import cats.effect.Bracket
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

class DoobieUserHistoryRepository[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable])
  extends UserHistoryRepository[F] with UserHistoryQueries {
  /** Inserts action to the user history repository */
  override def insertUserHistoryEntry(userHistory: UserAction): F[Unit] =
    insertUserHistory(userHistory).run.void.transact(transactor)

  /** Returns actions from the repository for the given user */
  override def getUserHistory(userId: UserId): F[List[UserAction]] =
    selectUserHistory(userId).to[List].transact(transactor)
}
