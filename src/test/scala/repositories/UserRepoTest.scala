package repositories

import java.sql.Timestamp

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.model.{User, UserReg}
import tech.cryptonomic.cloud.nautilus.repositories.UserRepoImpl

class UserRepoTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new UserRepoImpl()

  "UserRepo" should {
    "check creation of user" in {
      check(sut.createUser(UserReg("", "", "", new Timestamp(0), None, None)))
    }
    "check updating of user " in {
      check(sut.updateUser(User(0, "", "", "", new Timestamp(0), None, None)))
    }
    "check getUser" in {
      check(sut.getUser(0))
    }
  }

}
