package tech.cryptonomic.cloud.nautilus.repositories

import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.cloud.nautilus.model.{User, UserReg}

/** Trait representing Doobie User repo queries */
trait UserRepo {
  /** Creates user */
  def createUser(userReg: UserReg): Update0

  /** Updates user */
  def updateUser(user: User): Update0

  /** Returns user */
  def getUser(userId: Int): Query0[User]
}
