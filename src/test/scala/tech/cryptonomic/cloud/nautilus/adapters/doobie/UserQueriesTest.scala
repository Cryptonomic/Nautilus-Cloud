package tech.cryptonomic.cloud.nautilus.adapters.doobie

import java.time.Instant

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.InMemoryDatabase
import tech.cryptonomic.cloud.nautilus.domain.user.{AuthenticationProvider, CreateUser, Role, UpdateUser, User}

class UserQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new UserQueries {}

  "UserRepo" should {
    "check creation of user" in {
      check(sut.createUserQuery(CreateUser("name@domain.com", Role.User, Instant.now(), AuthenticationProvider.Github, None)))
    }
    "check updating of user " in {
      check(sut.updateUserQuery(1, UpdateUser("name@domain.com", Role.User, AuthenticationProvider.Github, None)))
    }
    "check getUser" in {
      check(sut.getUserQuery(0))
    }
    "check getUserByEmail" in {
      check(sut.getUserByEmailQuery("name@domain.com"))
    }
  }
}
