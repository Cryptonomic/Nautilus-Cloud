package tech.cryptonomic.cloud.nautilus.adapters.doobie

import java.time.Instant

import cats.effect.IO
import doobie.scalatest._
import org.scalatest._
import tech.cryptonomic.cloud.nautilus.InMemoryDatabase
import tech.cryptonomic.cloud.nautilus.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.cloud.nautilus.domain.user.{AuthenticationProvider, CreateUser, Role, UpdateUser, User}

class DoobieUserRepositoryTest
    extends WordSpec
    with Matchers
    with IOChecker
    with EitherValues
    with OptionValues
    with InMemoryDatabase {

  override def transactor: doobie.Transactor[IO] = testTransactor

  val now: Instant = Instant.now()

  val sut = new DoobieUserRepository[IO](transactor)

  "ApiKeyRepo" should {
      "save user" in {
        // when
        val id =
          sut
            .createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None))
            .unsafeRunSync()
            .right
            .value

        // then
        id should equal(1)
      }

      "shouldn't save user when user with a given email address already exists" in {
        // when
        val id = sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None)).unsafeRunSync

        // then
        id.isLeft shouldBe true
      }

      "fetch user" in {
        // when
        val fetchedUser = sut.getUser(1).unsafeRunSync().value

        // then
        fetchedUser should equal(User(1, "login@domain.com", Role.Administrator, now, Github, None))
      }

      "fetch user by email" in {
        // when
        val fetchedUser = sut.getUserByEmailAddress("login@domain.com").unsafeRunSync().value

        // then
        fetchedUser should equal(User(1, "login@domain.com", Role.Administrator, now, Github, None))
      }

      "update user" in {
        // when
        sut
          .updateUser(1, UpdateUser("different-login@domain.com", Role.User, AuthenticationProvider.Github, None))
          .unsafeRunSync()

        // when
        val fetchedUser = sut.getUser(1).unsafeRunSync().value

        // then
        fetchedUser should equal(User(1, "different-login@domain.com", Role.User, now, Github, None))
      }
    }
}
