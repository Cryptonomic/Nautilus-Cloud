package tech.cryptonomic.cloud.nautilus.repositories

import cats.effect.Bracket
import doobie._
import doobie.implicits._
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserReg}

import scala.language.higherKinds

//  users	                POST	Add new user
//  users                 PUT update user
//  users/{user}	        GET	Fetches user info
//  users/{user}/apiKeys	GET	Get all API keys for given user
//  users/{user}/usage	  GET	Gets the number of queries used by the given user
trait UserRepo[F[_]] {
  def createUser(userReg: UserReg): F[Int]

  def updateUser(user: User): F[Int]

  def getUser(userId: Long): F[Option[User]]

}

class UserRepoImpl[F[_]](transactor: Transactor[F])(implicit br: Bracket[F, Throwable]) extends UserRepo[F] {
  override def createUser(userReg: UserReg): F[Int] =
    sql"""INSERT INTO users (username, useremail, userrole, registrationdate, accountsource, accountdescription)
         |VALUES (${userReg.userName}, ${userReg.userEmail}, ${userReg.userRole},
         |${userReg.registrationDate}, ${userReg.accountSource}, ${userReg.accountDescription}) """.stripMargin.update.run
      .transact(transactor)

  override def updateUser(user: User): F[Int] =
    sql"""UPDATE users SET username = ${user.userName}, useremail = ${user.userEmail},
         |userrole = ${user.userRole}, registrationdate = ${user.registrationDate},
         |accountsource = ${user.accountSource}, accountdescription = ${user.accountDescription}
         |WHERE userid = ${user.userId}""".stripMargin.update.run
      .transact(transactor)

  override def getUser(userId: Long): F[Option[User]] =
    sql"SELECT userid, username, useremail, userrole, registrationdate, accountsource, accountdescription FROM users WHERE userid = $userId"
      .query[User]
      .option
      .transact(transactor)
}
