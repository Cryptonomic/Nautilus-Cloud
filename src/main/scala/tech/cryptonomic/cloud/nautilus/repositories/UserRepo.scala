package tech.cryptonomic.cloud.nautilus.repositories

import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.cloud.nautilus.model.{User, UserReg}

import scala.language.higherKinds

trait UserRepo {
  def createUser(userReg: UserReg): Update0

  def updateUser(user: User): Update0

  def getUser(userId: Int): Query0[User]
}
