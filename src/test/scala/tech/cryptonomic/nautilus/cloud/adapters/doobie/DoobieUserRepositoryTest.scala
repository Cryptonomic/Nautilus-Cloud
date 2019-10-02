package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.pagination.PaginatedResult
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, UpdateUser, User}
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, InMemoryDatabase}

class DoobieUserRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with InMemoryDatabase {

  val now: Instant = Instant.now()

  val sut = DefaultNautilusContext.userRepository

  "UserRepo" should {
      "save and receive user" in {
        // when
        val id =
          sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()

        // then
        id.right.value should equal(1)

        // when
        val fetchedUser = sut.getUser(1).unsafeRunSync()

        // then
        fetchedUser.value should equal(User(1, "login@domain.com", Role.Administrator, now, Github, None))
      }

      "shouldn't save user when user with a given email address already exists" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()

        // when
        val id = sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync

        // then
        id.isLeft shouldBe true
      }

      "fetch user by email" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()

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
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()

        // when
        sut
          .updateUser(1, UpdateUser(Role.User, Some("brand new description")))
          .unsafeRunSync()

        // and
        val fetchedUser = sut.getUser(1).unsafeRunSync()

        // then
        fetchedUser.value should equal(
          User(1, "login@domain.com", Role.User, now, Github, Some("brand new description"))
        )
      }

      "delete user" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()
        sut.getUser(1).unsafeRunSync() should not be empty

        // when
        sut.deleteUser(1, now).unsafeRunSync()

        // then
        sut.getUser(1).unsafeRunSync() shouldBe empty
        sut.getUserByEmailAddress("login@domain.com").unsafeRunSync() shouldBe empty
      }

      "get all users" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()
        sut.createUser(CreateUser("some-other-login@domain.com", Role.User, now, Github, 1, None)).unsafeRunSync()

        // when
        val users = sut.getUsers()().unsafeRunSync()

        // then
        users should equal(
          PaginatedResult(
            pagesTotal = 1,
            resultCount = 2,
            result = List(
              User(1, "login@domain.com", Role.Administrator, now, Github),
              User(2, "some-other-login@domain.com", Role.User, now, Github)
            )
          )
        )
      }

      "filter users by id" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()
        sut.createUser(CreateUser("some-other-login@domain.com", Role.User, now, Github, 1, None)).unsafeRunSync()

        // when
        val users = sut.getUsers(Some(1))().unsafeRunSync()

        // then
        users.result should equal(
          List(
            User(1, "login@domain.com", Role.Administrator, now, Github)
          )
        )
      }

      "filter users by email" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()
        sut.createUser(CreateUser("some-other-login@domain.com", Role.User, now, Github, 1, None)).unsafeRunSync()

        // expect
        sut
          .getUsers(email = Some("some-other-login@domain.com"))()
          .unsafeRunSync()
          .result
          .map(_.userEmail) should equal(
          List("some-other-login@domain.com")
        )

        sut.getUsers(email = Some("some-other"))().unsafeRunSync().result.map(_.userEmail) should equal(
          List("some-other-login@domain.com")
        )

        sut.getUsers(email = Some("domain"))().unsafeRunSync().result.map(_.userEmail) should equal(
          List("login@domain.com", "some-other-login@domain.com")
        )
      }
    }
}
