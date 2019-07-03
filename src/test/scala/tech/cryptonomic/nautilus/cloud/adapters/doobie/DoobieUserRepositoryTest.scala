package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, CreateUser, Role, UpdateUser, User}
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class DoobieUserRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with InMemoryDatabase {

  val now: Instant = Instant.now()

  val sut = NautilusContext.userRepository

  "UserRepo" should {
      "save and receive user" in {
        // when
        val id =
          sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None)).unsafeRunSync()

        // then
        id.right.value should equal(1)

        // when
        val fetchedUser = sut.getUser(1).unsafeRunSync()

        // then
        fetchedUser.value should equal(User(1, "login@domain.com", Role.Administrator, now, Github, None))
      }

      "shouldn't save user when user with a given email address already exists" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None)).unsafeRunSync()

        // when
        val id = sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None)).unsafeRunSync

        // then
        id.isLeft shouldBe true
      }

      "fetch user by email" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None)).unsafeRunSync()

        // when
        val fetchedUser = sut.getUserByEmailAddress("login@domain.com").unsafeRunSync()

        // then
        fetchedUser.value should equal(User(1, "login@domain.com", Role.Administrator, now, Github, None))
      }

      "return None when fetching non existing user" in {
        // expect
        sut.getUser(1).unsafeRunSync() shouldBe None
        sut.getUserByEmailAddress("login@domain.com").unsafeRunSync() shouldBe None
      }

      "update user" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None)).unsafeRunSync()

        // when
        sut
          .updateUser(1, UpdateUser("different-login@domain.com", Role.User, AuthenticationProvider.Github, None))
          .unsafeRunSync()

        // and
        val fetchedUser = sut.getUser(1).unsafeRunSync()

        // then
        fetchedUser.value should equal(User(1, "different-login@domain.com", Role.User, now, Github, None))
      }

    "get usage left for the user" in {
      // given
      sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, None)).unsafeRunSync()

      // when
      sut
        .updateUser(1, UpdateUser("different-login@domain.com", Role.User, AuthenticationProvider.Github, None))
        .unsafeRunSync()

      // and
      val fetchedUser = sut.getUser(1).unsafeRunSync()

      // then
      fetchedUser.value should equal(User(1, "different-login@domain.com", Role.User, now, Github, None))
    }

    }
}
