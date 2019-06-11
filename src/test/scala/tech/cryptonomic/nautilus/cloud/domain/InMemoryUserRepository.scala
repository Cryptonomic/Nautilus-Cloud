package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User, UserRepository}

import scala.collection.mutable

class InMemoryUserRepository extends UserRepository[Id] {

  /** list of all users
    *
    * in order to be consistent with a real database we adjust reads and writes to keep indexing starting from 1 not
    * from 0
    */
  val users = new mutable.MutableList[User]()

  /** Creates user */
  override def createUser(user: CreateUser): Either[Throwable, UserId] = this.synchronized {
    val userId = users.size + 1
    users.+=(user.toUser(userId))
    Right(userId)
  }

  /** Updates user */
  override def updateUser(id: UserId, user: UpdateUser): Unit = this.synchronized {
    users
      .find(_.userId == id)
      .map(
        _.copy(
          userEmail = user.userEmail,
          userRole = user.userRole,
          accountSource = user.accountSource,
          accountDescription = user.accountDescription
        )
      )
  }

  /** Returns user */
  override def getUser(id: UserId): Option[User] = this.synchronized {
    users.find(_.userId == id)
  }

  /** Returns user by email address */
  override def getUserByEmailAddress(email: String): Option[User] = this.synchronized {
    users.find(_.userEmail == email)
  }

  /** Clears repository */
  def clear(): Unit = this.synchronized {
    users.clear()
  }
}
