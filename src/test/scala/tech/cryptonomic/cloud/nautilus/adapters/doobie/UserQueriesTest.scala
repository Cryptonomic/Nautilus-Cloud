package tech.cryptonomic.cloud.nautilus.adapters.doobie

import java.sql.Timestamp

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.InMemoryDatabase
import tech.cryptonomic.cloud.nautilus.domain.user.{User, UserWithoutId}

class UserQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new UserQueries {}

  "UserRepo" should {
    "check creation of user" in {
      check(sut.createUserQuery(UserWithoutId("", "", new Timestamp(0), None, None)))
    }
    "check updating of user " in {
      check(sut.updateUserQuery(User(0, "", "", new Timestamp(0), None, None)))
    }
    "check getUser" in {
      check(sut.getUserQuery(0))
    }
  }
}
