package tech.cryptonomic.nautilus.cloud.application

import java.time.{Instant, ZonedDateTime}

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.authentication.{ConfirmRegistration, TosNotAcceptedException}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, User}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.IdContext

class AuthenticationApplicationTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with Fixtures
    with BeforeAndAfterEach
    with OneInstancePerTest {

  val context = new IdContext {
    override lazy val now: Instant = ZonedDateTime.parse("2019-05-27T12:03:48.081+01:00").toInstant
  }
  val authenticationApplication = context.authenticationApplication
  val authRepository = context.authRepository
  val userRepository = context.userRepository
  val apiKeyRepository = context.apiKeyRepository

  override protected def afterEach(): Unit =
    super.afterEach()

  "AuthenticationApplication" should {
      "resolve an auth code when user exist" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.createUser(
          exampleCreateUser.copy(
            userEmail = "name@domain.com",
            userRole = Role.User,
            registrationDate = context.now.minusSeconds(1),
            accountSource = Github,
            tierId = 1,
            tosAccepted = true,
            newsletterAccepted = false,
            registrationIp = None
          )
        )

        // expect
        authenticationApplication.resolveAuthCode("authCode").right.value.right.value shouldBe User(
          userId = 1,
          userEmail = "name@domain.com",
          userRole = Role.User,
          registrationDate = context.now.minusSeconds(1),
          accountSource = Github,
          tosAccepted = true,
          newsletterAccepted = false,
          newsletterAcceptedDate = None,
          accountDescription = None
        )
      }

      "resolve an auth code when user exists with administrator role" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.createUser(exampleCreateUser.copy(userEmail = "name@domain.com", userRole = Role.Administrator))

        // expect
        authenticationApplication.resolveAuthCode("authCode").right.value.right.value should have(
          'userId (1),
          'userEmail ("name@domain.com"),
          'userRole (Role.Administrator)
        )
      }

      "create an user when the user with a given email doesn't exist and generate keys and usage for him" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.getUser(1) should be(None)

        // when
        val registrationAttemptId = authenticationApplication.resolveAuthCode("authCode").right.value.left.value
        val user = authenticationApplication
          .acceptRegistration(ConfirmRegistration(registrationAttemptId = registrationAttemptId, tosAccepted = true))
          .right
          .value

        // then
        user shouldEqual userRepository.getUser(1).value
        user should have(
          'userId (1),
          'userEmail ("name@domain.com"),
          'userRole (Role.User),
          'accountSource (Github)
        )
        apiKeyRepository.getUserApiKeys(1).size shouldBe 2
        apiKeyRepository.getKeysUsageForUser(1).size shouldBe 2
      }

      "return Left when a given auth code shouldn't be resolved" in {
        // expect
        authenticationApplication.resolveAuthCode("authCode").left.value
      }

      "not process when terms of conditions weren't accepted" in {
        // expect
        authenticationApplication
          .acceptRegistration(exampleConfirmRegistration.copy(tosAccepted = false))
          .left
          .value shouldBe TosNotAcceptedException()
      }
    }
}
