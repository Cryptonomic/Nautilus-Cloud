package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Applicative
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.CreateUser
import tech.cryptonomic.nautilus.cloud.domain.user.UpdateUser
import tech.cryptonomic.nautilus.cloud.domain.user.User
import tech.cryptonomic.nautilus.cloud.domain.user.UserRepository

import scala.language.higherKinds

class InMemoryUserRepository[F[_]: Applicative] extends UserRepository[F] {

  /** list of all users
    *
    * in order to be consistent with a real database we adjust reads and writes to keep indexing starting from 1 not
    * from 0
    */
  private var users: List[User] = List.empty

  /** Creates user */
  override def createUser(user: CreateUser): F[Either[Throwable, UserId]] = this.synchronized {
    val userId = users.size + 1
    users = users :+ user.toUser(userId)
    userId.asRight[Throwable].pure[F]
  }

  /** Updates user */
  override def updateUser(id: UserId, user: UpdateUser): F[Unit] = this.synchronized {
    users = users.collect {
      case it if it.userId == id =>
        it.copy(
          userRole = user.userRole,
          accountDescription = user.accountDescription
        )
      case it => it
    }
    ().pure[F]
  }

  /** Returns user */
  override def getUser(id: UserId): F[Option[User]] = this.synchronized {
    users.find(_.userId == id).pure[F]
  }

  /** Returns user by email address */
  override def getUserByEmailAddress(email: String): F[Option[User]] = this.synchronized {
    users.find(_.userEmail == email).pure[F]
  }

  /** Clears repository */
  def clear(): Unit = this.synchronized {
    users = List.empty
  }
}
