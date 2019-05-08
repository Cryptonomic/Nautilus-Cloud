package repositories.dao

import java.sql.Timestamp

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import repositories.InMemoryDatabase
import tech.cryptonomic.cloud.nautilus.model.{User, UserWithoutId}
import tech.cryptonomic.cloud.nautilus.repositories.dao.UserDao

class UserDaoTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new UserDao {}

  "UserRepo" should {
    "check creation of user" in {
      check(sut.createUserQuery(UserWithoutId("", "", "", new Timestamp(0), None, None)))
    }
    "check updating of user " in {
      check(sut.updateUserQuery(User(0, "", "", "", new Timestamp(0), None, None)))
    }
    "check getUser" in {
      check(sut.getUserQuery(0))
    }
  }
}
