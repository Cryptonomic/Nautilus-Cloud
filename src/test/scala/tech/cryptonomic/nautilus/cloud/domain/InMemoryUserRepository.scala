package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User, UserRepository}

class InMemoryUserRepository extends UserRepository[Id] {
  val users = new scala.collection.mutable.MutableList[User]()

  /** Creates user */
  override def createUser(user: CreateUser): Either[Throwable, UserId] = this.synchronized {
    users.+=(user.toUser(users.size + 1))
    Right(users.size)
  }

  /** Updates user */
  override def updateUser(id: UserId, user: UpdateUser): Unit = this.synchronized {
    users
      .find(_.userId == id + 1) // indexing starts with 1 instead of 0
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
    users.get(id.toInt - 1) // indexing starts with 1 instead of 0
  }

  /** Returns user by email address */
  override def getUserByEmailAddress(email: String): Option[User] = this.synchronized {
    users.find(_.userEmail == email)
  }

  def clear(): Unit = users.clear()
}
