package tech.cryptonomic.cloud.nautilus.repositories.dao

import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.cloud.nautilus.model.{User, UserWithoutId}
import doobie.implicits._

trait UserDao {

  /** Creates user */
  def createUserQuery(userReg: UserWithoutId): Update0 =
    sql"""INSERT INTO users (username, useremail, userrole, registrationdate, accountsource, accountdescription)
         |VALUES (${userReg.userName}, ${userReg.userEmail}, ${userReg.userRole},
         |${userReg.registrationDate}, ${userReg.accountSource}, ${userReg.accountDescription})""".stripMargin.update

  /** Updates user */
  def updateUserQuery(user: User): Update0 =
    sql"""UPDATE users SET username = ${user.userName}, useremail = ${user.userEmail},
         |userrole = ${user.userRole}, registrationdate = ${user.registrationDate},
         |accountsource = ${user.accountSource}, accountdescription = ${user.accountDescription}
         |WHERE userid = ${user.userId}""".stripMargin.update

  /** Returns user */
  def getUserQuery(userId: Int): Query0[User] =
    sql"SELECT userid, username, useremail, userrole, registrationdate, accountsource, accountdescription FROM users WHERE userid = $userId"
      .query[User]
}
