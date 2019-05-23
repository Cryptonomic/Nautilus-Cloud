package tech.cryptonomic.cloud.nautilus.adapters.doobie

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.cloud.nautilus.domain.user.{User, UserWithoutId}

trait UserQueries {

  /** Creates user */
  def createUserQuery(userReg: UserWithoutId): Update0 =
    sql"""INSERT INTO users (useremail, userrole, registrationdate, accountsource, accountdescription)
         |VALUES (${userReg.userEmail}, ${userReg.userRole},${userReg.registrationDate}, ${userReg.accountSource},
         |${userReg.accountDescription})""".stripMargin.update

  /** Updates user */
  def updateUserQuery(user: User): Update0 =
    sql"""UPDATE users SET useremail = ${user.userEmail}, |userrole = ${user.userRole},
         |registrationdate = ${user.registrationDate}, accountsource = ${user.accountSource},
         |accountdescription = ${user.accountDescription} WHERE userid = ${user.userId}""".stripMargin.update

  /** Returns user */
  def getUserQuery(userId: Int): Query0[User] =
    sql"SELECT userid, useremail, userrole, registrationdate, accountsource, accountdescription FROM users WHERE userid = $userId"
      .query[User]
}
