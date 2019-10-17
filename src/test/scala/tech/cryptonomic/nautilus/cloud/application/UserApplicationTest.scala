package tech.cryptonomic.nautilus.cloud.application

import cats.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AccessDenied, ConfirmRegistration}
import tech.cryptonomic.nautilus.cloud.domain.pagination.Pagination
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
          CreateUser(
            userEmail = "user@domain.com",
            userRole = Role.Administrator,
            registrationDate = time,
            accountSource = AuthenticationProvider.Github,
            tierId = 1,
            tosAccepted = true,
            newsletterAccepted = false
          )
        )

        // expect
        sut.getUser(1)(adminSession).right.value.value shouldBe User(
          userId = 1,
          userEmail = "user@domain.com",
          userRole = Role.Administrator,
          registrationDate = time,
          accountSource = AuthenticationProvider.Github,
          tosAccepted = true,
          newsletterAccepted = false,
          newsletterAcceptedDate = None,
          accountDescription = None
        )
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
          CreateUser(
            userEmail = "user@domain.com",
            userRole = Role.Administrator,
            registrationDate = time,
            accountSource = AuthenticationProvider.Github,
            tierId = 1,
            tosAccepted = true,
            newsletterAccepted = false
          )
        )

        // expect
        sut.getCurrentUser(adminSession.copy(email = "user@domain.com")).value shouldBe User(
          userId = 1,
          userEmail = "user@domain.com",
          userRole = Role.Administrator,
          registrationDate = time,
          accountSource = AuthenticationProvider.Github,
          tosAccepted = true,
          newsletterAccepted = false,
          newsletterAcceptedDate = None,
          accountDescription = None
        )
      }

      "get users" in {
        // given
        userRepository.createUser(exampleCreateUser.copy(userEmail = "user1@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "user2@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "user3@domain.com"))

        // when
        val users = sut.getUsers()(Pagination(2, 2))(adminSession).right.value

        // expect
        users.pagesTotal shouldBe 2
        users.resultCount shouldBe 3
        users.result.map(_.userEmail) shouldBe List("user3@domain.com")
      }

      "get None when there is no current user" in {
        // expect
        sut.getCurrentUser(adminSession.copy(email = "non-existing-user@domain.com")) shouldBe None
      }

      "update user" in {
        // given
        userRepository.createUser(
          exampleCreateUser.copy(
            userRole = Role.Administrator,
            accountDescription = None
          )
        )

        // when
        sut.updateUser(1, AdminUpdateUser(Role.User.some, "some description".some))(adminSession)

        // then
        sut.getUser(1)(adminSession).right.value.value should have(
          'userRole (Role.User),
          'accountDescription (Some("some description"))
        )
      }

      "update current user" in {
        // given
        userRepository.createUser(
          exampleCreateUser.copy(
            newsletterAccepted = false,
            accountDescription = None
          )
        )

        // when
        sut.updateCurrentUser(UpdateCurrentUser(newsletterAccepted = Some(true), Some("some description")))(userSession)

        // then
        sut.getUser(1)(adminSession).right.value.value should have(
          'newsletterAccepted (true),
          'newsletterAcceptedDate (context.now.some),
          'accountDescription (Some("some description"))
        )
      }

      "delete user" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        val registrationAttemptId = authService.resolveAuthCode("authCode").right.value.left.value
        authService.acceptRegistration(
          ConfirmRegistration(registrationAttemptId = registrationAttemptId, tosAccepted = true)
        )

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
        // expect
        sut
          .updateUser(1, AdminUpdateUser(Role.User.some, "some description".some))(userSession)
          .left
          .value shouldBe a[AccessDenied]
      }
    }
}
