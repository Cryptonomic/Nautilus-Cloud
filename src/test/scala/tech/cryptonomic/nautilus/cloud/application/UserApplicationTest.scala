package tech.cryptonomic.nautilus.cloud.application

import org.scalamock.scalatest.MockFactory
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.domain.user._
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.IdContext

class UserApplicationTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach
    with MockFactory
    with OneInstancePerTest {

  val context = new IdContext()

  val apiKeyRepository = context.apiKeyRepository
  val userRepository = context.userRepository
  val authRepository = context.authRepository

  val apiKeyService = context.apiKeyService
  val authService = context.authenticationService

  val sut = context.userApplication

  "UserService" should {
      "get existing user" in {
        // given
        userRepository.createUser(
          CreateUser("user@domain.com", Role.Administrator, time, AuthenticationProvider.Github, 1)
        )

        // expect
        sut
          .getUser(1)(adminSession)
          .right
          .value
          .value shouldBe User(1, "user@domain.com", Role.Administrator, time, AuthenticationProvider.Github, None)
      }

      "get None when there is no existing user" in {
        // expect
        sut.getUser(1)(adminSession).right.value shouldBe None
      }

      "get PermissionDenied when requesting user is not an admin" in {
        // expect
        sut.getUser(1)(userSession).left.value shouldBe a[AccessDenied]
      }

      "get current user" in {
        // given
        userRepository.createUser(
          CreateUser("user@domain.com", Role.Administrator, time, AuthenticationProvider.Github, 1)
        )

        // expect
        sut
          .getCurrentUser(adminSession.copy(email = "user@domain.com"))
          .value shouldBe User(1, "user@domain.com", Role.Administrator, time, AuthenticationProvider.Github, None)
      }

      "get None when there is no current user" in {
        // expect
        sut.getCurrentUser(adminSession.copy(email = "non-existing-user@domain.com")) shouldBe None
      }

      "update user" in {
        // given
        userRepository.createUser(
          CreateUser("user@domain.com", Role.Administrator, time, AuthenticationProvider.Github, 1)
        )

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

      "delete user" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        authService.resolveAuthCode("authCode")

        sut.getCurrentUser(userSession.copy(email = "name@domain.com")) should not be empty
        apiKeyService.getUserApiKeys(1) should not be empty

        // when
        sut.deleteCurrentUser(userSession.copy(email = "name@domain.com"))

        // then
        sut.getCurrentUser(userSession.copy(email = "name@domain.com")) shouldBe empty
        apiKeyService.getUserApiKeys(1) shouldBe empty
      }

      "get PermissionDenied on deleting user when requesting user in admin" in {
        // expect
        sut.deleteCurrentUser(adminSession).left.value shouldBe a[AccessDenied]
      }

      "get PermissionDenied on updating user when requesting user is not an admin" in {
        // when
        sut.updateUser(1, UpdateUser(Role.User, Some("some description")))(adminSession)

        // then
        sut.getUser(1)(userSession).left.value shouldBe a[AccessDenied]
      }
    }
}
