package tech.cryptonomic.cloud.nautilus.repositories

import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.cloud.nautilus.model.{User, UserReg}
import doobie.implicits._

class UserRepoImpl extends UserRepo {
  override def createUser(userReg: UserReg): Update0 =
    sql"""INSERT INTO users (username, useremail, userrole, registrationdate, accountsource, accountdescription)
         |VALUES (${userReg.userName}, ${userReg.userEmail}, ${userReg.userRole},
         |${userReg.registrationDate}, ${userReg.accountSource}, ${userReg.accountDescription}) """.stripMargin.update

  override def updateUser(user: User): Update0 =
    sql"""UPDATE users SET username = ${user.userName}, useremail = ${user.userEmail},
         |userrole = ${user.userRole}, registrationdate = ${user.registrationDate},
         |accountsource = ${user.accountSource}, accountdescription = ${user.accountDescription}
         |WHERE userid = ${user.userId}""".stripMargin.update

  override def getUser(userId: Int): Query0[User] =
    sql"SELECT userid, username, useremail, userrole, registrationdate, accountsource, accountdescription FROM users WHERE userid = $userId"
      .query[User]
}
