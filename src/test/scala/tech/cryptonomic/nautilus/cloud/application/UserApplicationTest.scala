package tech.cryptonomic.nautilus.cloud.application

import org.scalamock.scalatest.MockFactory
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.domain.pagination.{PaginatedResult, Pagination}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
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

  "UserApplication" should {
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

      "get users" in {
        // given
        userRepository.createUser(exampleCreateUser.copy(userEmail = "user1@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "user2@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "user3@domain.com"))

        // expect
        sut
          .getUsers()(Pagination(2, 2))(adminSession)
          .right
          .value shouldBe
          PaginatedResult(
            pagesTotal = 2,
            resultCount = 3,
            result = List(User(3, "user3@domain.com", Role.User, time, Github))
          )
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
        val registrationAttemptId = authService.resolveAuthCode("authCode").right.value.left.value
        authService.acceptRegistration(registrationAttemptId)

        sut.getCurrentUser(userSession.copy(email = "name@domain.com")) should not be empty
        apiKeyService.getUserApiKeys(1) should not be empty

        // when
        sut.deleteCurrentUser(userSession.copy(email = "name@domain.com"))

        // then
        sut.getCurrentUser(userSession.copy(email = "name@domain.com")) shouldBe empty
        apiKeyService.getUserApiKeys(1) shouldBe empty
      }

      "get PermissionDenied on deleting current user when requesting user is an admin" in {
        // expect
        sut.deleteCurrentUser(adminSession).left.value shouldBe a[AccessDenied]
      }

      "get PermissionDenied on deleting user when requesting user is not an admin" in {
        // expect
        sut.deleteUser(1)(userSession).left.value shouldBe a[AccessDenied]
      }

      "get PermissionDenied on updating user when requesting user is not an admin" in {
        // when
        sut.updateUser(1, UpdateUser(Role.User, Some("some description")))(adminSession)

        // then
        sut.getUser(1)(userSession).left.value shouldBe a[AccessDenied]
      }
    }
}
