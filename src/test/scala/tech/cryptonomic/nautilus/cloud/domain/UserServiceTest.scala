package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.{InMemoryApiKeyRepository, InMemoryUserRepository}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.domain.user._
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class UserServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach
    with MockFactory {

  val apiKeyRepository = new InMemoryApiKeyRepository[Id]()
  val userRepository = new InMemoryUserRepository[Id]()

  val sut = new UserService[Id](userRepository, apiKeyRepository)

  override protected def afterEach(): Unit = {
    super.afterEach()
    userRepository.clear()
  }

  "UserService" should {
      "get existing user" in {
        // given
        userRepository.createUser(CreateUser("user@domain.com", Role.Administrator, time, AuthenticationProvider.Github))

        // expect
        sut
          .getUser(1)(adminSession)
          .right
          .value
          .value shouldBe User(1, "user@domain.com", Role.Administrator, time, AuthenticationProvider.Github, None)
      }

      "get None when there is no existing user" in {
        // expect
        sut.getUser(1)(adminSession)
          .right
          .value shouldBe None
      }

      "get PermissionDenied when requesting user is not an admin" in {
        // expect
        sut.getUser(1)(userSession)
          .left
          .value shouldBe a[AccessDenied]
      }

      "get current user" in {
        // given
        userRepository.createUser(CreateUser("user@domain.com", Role.Administrator, time, AuthenticationProvider.Github))

        // expect
        sut
          .getCurrentUser(adminSession.copy(email = "user@domain.com"))
          .value shouldBe User(1, "user@domain.com", Role.Administrator, time, AuthenticationProvider.Github, None)
      }

      "get None when there is no current user" in {
        // expect
        sut.getCurrentUser(adminSession.copy("non-existing-user@domain.com")) shouldBe None
      }

      "update user" in {
        // given
        userRepository.createUser(CreateUser("user@domain.com", Role.Administrator, time, AuthenticationProvider.Github))

        // when
        sut.updateUser(1, UpdateUser(Role.User, Some("some description")))(adminSession)

        // then
        sut.getUser(1)(adminSession).right.value.value shouldBe User(
          1,
          "user@domain.com",
          Role.User,
          time,
          AuthenticationProvider.Github,
          Some("some description")
        )
      }

      "get PermissionDenied on updating user when requesting user is not an admin" in {
        // when
        sut.updateUser(1, UpdateUser(Role.User, Some("some description")))(adminSession)

        // then
        sut.getUser(1)(userSession).left.value shouldBe a[AccessDenied]
      }

      "getUserApiKeys" in {
        // given
        apiKeyRepository.add(exampleApiKey.copy(keyId = 1, userId = 1))
        apiKeyRepository.add(exampleApiKey.copy(keyId = 2, userId = 1))
        apiKeyRepository.add(exampleApiKey.copy(keyId = 3, userId = 2))

        //
        sut.getUserApiKeys(1)(adminSession).right.value.map(_.keyId) shouldBe List(1, 2)
      }
    }
}
