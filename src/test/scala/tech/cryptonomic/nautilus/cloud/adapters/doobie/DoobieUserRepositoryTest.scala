package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.pagination.{PaginatedResult, Pagination}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, UpdateUser, User}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, InMemoryDatabase}

class DoobieUserRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with Fixtures
    with InMemoryDatabase {

  val now: Instant = Instant.now()

  private val context: DefaultNautilusContext.type = DefaultNautilusContext

  val sut = context.userRepository
  val apiKeyRepository = context.apiKeyRepository

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

      "filter users by api keys" in {
        // given
        sut.createUser(CreateUser("login@domain.com", Role.Administrator, now, Github, 1, None)).unsafeRunSync()
        sut.createUser(CreateUser("some-other-login@domain.com", Role.User, now, Github, 1, None)).unsafeRunSync()

        apiKeyRepository.putApiKey(exampleCreateApiKey.copy(userId = 1, key = "some-api-key-1")).unsafeRunSync()
        apiKeyRepository.putApiKey(exampleCreateApiKey.copy(userId = 2, key = "some-api-key-2")).unsafeRunSync()

        // expect
        sut
          .getUsers(apiKey = Some("key-1"))()
          .unsafeRunSync()
          .result
          .map(_.userEmail) should equal(List("login@domain.com"))
      }

      "paginate users" in {
        // given
        sut.createUser(exampleCreateUser.copy(userEmail = "login1@domain.com")).unsafeRunSync()
        sut.createUser(exampleCreateUser.copy(userEmail = "login2@domain.com")).unsafeRunSync()

        // when
        val resultPage1 = sut.getUsers()(Pagination(1, 1)).unsafeRunSync()

        // then
        resultPage1.pagesTotal shouldEqual 2
        resultPage1.resultCount shouldEqual 2
        resultPage1.result.map(_.userEmail) should equal(List("login1@domain.com"))

        // when
        val resultPage2 = sut.getUsers()(Pagination(1, 2)).unsafeRunSync()

        // then
        resultPage2.pagesTotal shouldEqual 2
        resultPage2.resultCount shouldEqual 2
        resultPage2.result.map(_.userEmail) should equal(List("login2@domain.com"))

        // when
        val resultPage3 = sut.getUsers()(Pagination(1, 3)).unsafeRunSync()

        // then
        resultPage3.pagesTotal shouldEqual 2
        resultPage3.resultCount shouldEqual 2
        resultPage3.result shouldBe empty
      }
    }
}
