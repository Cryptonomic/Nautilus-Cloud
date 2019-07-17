package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.OptionValues
import org.scalatest.EitherValues
import org.scalatest.Matchers
import org.scalatest.WordSpec
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryApiKeyRepository
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryUserRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.PermissionDenied
import tech.cryptonomic.nautilus.cloud.domain.resources.ResourceRepository
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.domain.user.CreateUser
import tech.cryptonomic.nautilus.cloud.domain.user.UpdateUser
import tech.cryptonomic.nautilus.cloud.domain.user.User
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
  val resourceRepository = stub[ResourceRepository[Id]]

  val sut = new UserService[Id](userRepository, apiKeyRepository, resourceRepository)

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
          .value shouldBe a[PermissionDenied]
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
        sut.getUser(1)(userSession).left.value shouldBe a[PermissionDenied]
      }

      "getUserApiKeys" in {
        // given
        apiKeyRepository.add(exampleApiKey.copy(keyId = 1, userId = 1))
        apiKeyRepository.add(exampleApiKey.copy(keyId = 2, userId = 1))
        apiKeyRepository.add(exampleApiKey.copy(keyId = 3, userId = 2))

        //
        sut.getUserApiKeys(1).map(_.keyId) shouldBe List(1, 2)
      }
    }
}
