package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.pagination.Pagination
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
          sut
            .createUser(
              CreateUser(
                userEmail = "login@domain.com",
                userRole = Role.Administrator,
                registrationDate = now,
                accountSource = Github,
                tierId = 1,
                tosAccepted = true,
                newsletterAccepted = true,
                registrationIp = None
              )
            )
            .unsafeRunSync()

        // then
        id.right.value should equal(1)

        // when
        val fetchedUser = sut.getUser(1).unsafeRunSync()

        // then
        fetchedUser.value should equal(
          User(
            userId = 1,
            userEmail = "login@domain.com",
            userRole = Role.Administrator,
            registrationDate = now,
            accountSource = Github,
            tosAccepted = true,
            newsletterAccepted = true,
            newsletterAcceptedDate = Some(now),
            accountDescription = None
          )
        )
      }

      "shouldn't save user when user with a given email address already exists" in {
        // given
        sut.createUser(exampleCreateUser.copy(userEmail = "login@domain.com")).unsafeRunSync()

        // when
        val id = sut.createUser(exampleCreateUser.copy(userEmail = "login@domain.com")).unsafeRunSync

        // then
        id.isLeft shouldBe true
      }

      "fetch user by email" in {
        // given
        sut
          .createUser(
            CreateUser(
              userEmail = "login@domain.com",
              userRole = Role.Administrator,
              registrationDate = now,
              accountSource = Github,
              tierId = 1,
              tosAccepted = true,
              newsletterAccepted = false,
              registrationIp = None
            )
          )
          .unsafeRunSync()

        // when
        val fetchedUser = sut.getUserByEmailAddress("login@domain.com").unsafeRunSync()

        // then
        fetchedUser.value should equal(
          User(
            userId = 1,
            userEmail = "login@domain.com",
            userRole = Role.Administrator,
            registrationDate = now,
            accountSource = Github,
            tosAccepted = true,
            newsletterAccepted = false,
            newsletterAcceptedDate = None,
            accountDescription = None
          )
        )
      }

      "return None when fetching non existing user" in {
        // expect
        sut.getUser(1).unsafeRunSync() shouldBe None
        sut.getUserByEmailAddress("login@domain.com").unsafeRunSync() shouldBe None
      }

      "update user" in {
        // given
        sut
          .createUser(
            exampleCreateUser.copy(userRole = Role.Administrator, newsletterAccepted = false, accountDescription = None)
          )
          .unsafeRunSync()

        // when
        sut
          .updateUser(
            1,
            UpdateUser(
              userRole = Role.User.some,
              newsletterAccepted = true.some,
              accountDescription = "brand new description".some
            ),
            now
          )
          .unsafeRunSync()

        // and
        val fetchedUser = sut.getUser(1).unsafeRunSync()

        // then
        fetchedUser.value should have(
          'userRole (Role.User),
          'newsletterAccepted (true),
          'newsletterAcceptedDate (now.some),
          'accountDescription (Some("brand new description"))
        )
      }

      "delete user" in {
        // given
        sut.createUser(exampleCreateUser.copy(userEmail = "login@domain.com")).unsafeRunSync()
        sut.getUser(1).unsafeRunSync() should not be empty

        // when
        sut.deleteUser(1, now).unsafeRunSync()

        // then
        sut.getUser(1).unsafeRunSync() shouldBe empty
        sut.getUserByEmailAddress("login@domain.com").unsafeRunSync() shouldBe empty
      }

      "get all users" in {
        // given
        sut.createUser(exampleCreateUser.copy(userEmail = "login@domain.com")).unsafeRunSync()
        sut.createUser(exampleCreateUser.copy(userEmail = "some-other-login@domain.com")).unsafeRunSync()

        // when
        val users = sut.getUsers()().unsafeRunSync()

        // then
        users.pagesTotal should equal(1)
        users.resultCount should equal(2)
        users.result.map(_.userEmail) should equal(List("login@domain.com", "some-other-login@domain.com"))
      }

      "filter users by id" in {
        // given
        sut.createUser(exampleCreateUser.copy(userEmail = "login@domain.com")).unsafeRunSync()
        sut.createUser(exampleCreateUser.copy(userEmail = "some-other-login@domain.com")).unsafeRunSync()

        // when
        val users = sut.getUsers(SearchCriteria(userId = Some(1)))().unsafeRunSync()

        // then
        users.result.map(_.userEmail) should equal(List("login@domain.com"))
      }

      "filter users by email" in {
        // given
        sut.createUser(exampleCreateUser.copy(userEmail = "login@domain.com")).unsafeRunSync()
        sut.createUser(exampleCreateUser.copy(userEmail = "some-other-login@domain.com")).unsafeRunSync()

        // expect
        sut
          .getUsers(SearchCriteria(email = Some("some-other-login@domain.com")))()
          .unsafeRunSync()
          .result
          .map(_.userEmail) should equal(
          List("some-other-login@domain.com")
        )

        sut.getUsers(SearchCriteria(email = Some("some-other")))().unsafeRunSync().result.map(_.userEmail) should equal(
          List("some-other-login@domain.com")
        )

        sut.getUsers(SearchCriteria(email = Some("domain")))().unsafeRunSync().result.map(_.userEmail) should equal(
          List("login@domain.com", "some-other-login@domain.com")
        )
      }

      "filter users by api keys" in {
        // given
        sut.createUser(exampleCreateUser.copy(userEmail = "login@domain.com")).unsafeRunSync()
        sut.createUser(exampleCreateUser.copy(userEmail = "some-other-login@domain.com")).unsafeRunSync()

        apiKeyRepository.putApiKey(exampleCreateApiKey.copy(userId = 1, key = "some-api-key-1")).unsafeRunSync()
        apiKeyRepository.putApiKey(exampleCreateApiKey.copy(userId = 2, key = "some-api-key-2")).unsafeRunSync()

        // expect
        sut
          .getUsers(SearchCriteria(apiKey = Some("key-1")))()
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
        val resultPage2 = sut.getUsers()(Pagination(2, 1)).unsafeRunSync()

        // then
        resultPage2.pagesTotal shouldEqual 2
        resultPage2.resultCount shouldEqual 2
        resultPage2.result.map(_.userEmail) should equal(List("login2@domain.com"))

        // when
        val resultPage3 = sut.getUsers()(Pagination(3, 1)).unsafeRunSync()

        // then
        resultPage3.pagesTotal shouldEqual 2
        resultPage3.resultCount shouldEqual 2
        resultPage3.result shouldBe empty
      }
    }
}
