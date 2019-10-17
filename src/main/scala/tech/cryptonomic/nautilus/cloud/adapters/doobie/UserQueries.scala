package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import doobie._
import doobie.implicits._
import doobie.util.fragments.{setOpt, whereAndOpt}
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.pagination.Pagination
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
    sql"""INSERT INTO users (useremail, userrole, registrationdate, accountsource, tosAccepted, newsletterAccepted,
         |newsletterAcceptedDate, registrationip, accountdescription)
         |VALUES (${userReg.userEmail}, ${userReg.userRole},${userReg.registrationDate}, ${userReg.accountSource},
         |${userReg.tosAccepted}, ${userReg.newsletterAccepted}, ${userReg.newsletterAcceptedDate},
         |${userReg.registrationIp}, ${userReg.accountDescription})""".stripMargin.update

  /** Updates user */
  def updateUserQuery(id: UserId, user: UpdateUser, now: => Instant): Update0 = {

    val setFragment = setOpt(
      user.userRole.map(role => fr"userrole = $role"),
      user.newsletterAccepted.map(newsletterAccepted => fr"newsletterAccepted = $newsletterAccepted"),
      user.newsletterAcceptedDate(now).map(newsletterAcceptedDate => fr"newsletterAcceptedDate = $newsletterAcceptedDate"),
      user.accountDescription.map(accountDescription => fr"accountdescription = $accountDescription")
    )
    (fr"UPDATE users" ++ setFragment ++ fr"WHERE userid = $id and deleteddate is null").update
  }

  /** Deletes user */
  def deleteUserQuery(id: UserId, now: Instant): Update0 =
    sql"UPDATE users SET useremail = $deletedEmailHash, deleteddate = $now WHERE userid = $id".update

  /** Returns user */
  def getUserQuery(userId: UserId): Query0[User] =
    sql"SELECT userid, useremail, userrole, registrationdate, accountsource, tosAccepted, newsletterAccepted, newsletterAcceptedDate, accountdescription FROM users WHERE userid = $userId and deleteddate is null".query[User]

  /** Returns user by email address */
  def getUserByEmailQuery(email: String): Query0[User] =
    sql"SELECT userid, useremail, userrole, registrationdate, accountsource, tosAccepted, newsletterAccepted, newsletterAcceptedDate, accountdescription FROM users WHERE useremail = $email and deleteddate is null"
      .query[User]

  /** Returns filtered users */
  def getUsersQuery(searchCriteria: SearchCriteria)(pagination: Pagination): Query0[User] =
    (fr"SELECT userid, useremail, userrole, registrationdate, accountsource, tosAccepted, newsletterAccepted, newsletterAcceptedDate, accountdescription FROM users" ++
        toWhereSection(searchCriteria) ++
        fr"LIMIT ${pagination.pageSize.toLong} OFFSET ${pagination.offset.toLong}")
      .query[User]

  /** Returns count for users */
  def getUsersCountQuery(searchCriteria: SearchCriteria): Query0[Long] =
    (fr"SELECT count(*) FROM users" ++ toWhereSection(searchCriteria)).query[Long]

  private def toWhereSection(searchCriteria: SearchCriteria) = whereAndOpt(
    searchCriteria.userId.map(userId => fr"userid = $userId"),
    searchCriteria.email.map("%" + _ + "%").map(email => fr"useremail LIKE $email"),
    searchCriteria.apiKey
      .map("%" + _ + "%")
      .map(
        apiKey =>
          fr"EXISTS (SELECT 1 FROM api_keys WHERE api_keys.userid = users.userid AND key LIKE $apiKey AND datesuspended IS NULL)"
      )
  )

  private def deletedEmailHash: String = "deleted-mail-" + Random.alphanumeric.take(6).mkString
}
