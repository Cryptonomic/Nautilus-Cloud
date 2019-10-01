package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAndOpt
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, CreateUser, Role, UpdateUser, User}

import scala.util.Random

/** Trait containing User related queries */
trait UserQueries {

  implicit val authProviderGet: Get[AuthenticationProvider] = Get[String].tmap(AuthenticationProvider.byName)
  implicit val authProviderPut: Put[AuthenticationProvider] = Put[String].tcontramap(_.name)

  implicit val roleGet: Get[Role] = Get[String].tmap(Role.byName)
  implicit val rolePut: Put[Role] = Put[String].tcontramap(_.name)

  /** Creates user */
  def createUserQuery(userReg: CreateUser): Update0 =
    sql"""INSERT INTO users (useremail, userrole, registrationdate, accountsource, accountdescription)
         |VALUES (${userReg.userEmail}, ${userReg.userRole},${userReg.registrationDate}, ${userReg.accountSource},
         |${userReg.accountDescription})""".stripMargin.update

  /** Updates user */
  def updateUserQuery(id: UserId, user: UpdateUser): Update0 =
    sql"UPDATE users SET userrole = ${user.userRole}, accountdescription = ${user.accountDescription} WHERE userid = $id and deleteddate is null".update

  /** Deletes user */
  def deleteUserQuery(id: UserId, now: Instant): Update0 =
    sql"""UPDATE users SET useremail = $deletedEmailHash, deleteddate = $now WHERE userid = $id""".stripMargin.update

  /** Returns user */
  def getUserQuery(userId: UserId): Query0[User] =
    sql"SELECT userid, useremail, userrole, registrationdate, accountsource, accountdescription FROM users WHERE userid = $userId and deleteddate is null"
      .query[User]

  /** Returns user by email address */
  def getUserByEmailQuery(email: String): Query0[User] =
    sql"SELECT userid, useremail, userrole, registrationdate, accountsource, accountdescription FROM users WHERE useremail = $email and deleteddate is null"
      .query[User]

  /** Returns all users */
  def getUsersQuery(userId: Option[UserId], email: Option[Email]): Query0[User] = {

    val userIdPart = userId.map(userId => fr"userid = $userId")
    val emailPart = email.map("%" + _ + "%").map(email => fr"useremail LIKE $email")

    (fr"SELECT userid, useremail, userrole, registrationdate, accountsource, accountdescription FROM users" ++
        whereAndOpt(userIdPart, emailPart))
      .query[User]
  }

  private def deletedEmailHash: String = "deleted-mail-" + Random.alphanumeric.take(6).mkString
}
