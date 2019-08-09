package tech.cryptonomic.nautilus.cloud.application

import java.time.{Instant, ZonedDateTime}

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role}
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
    override lazy val now = ZonedDateTime.parse("2019-05-27T12:03:48.081+01:00").toInstant
  }
  val authenticationApplication = context.authenticationApplication
  val authRepository = context.authRepository
  val userRepository = context.userRepository
  val apiKeyRepository = context.apiKeyRepository

  override protected def afterEach(): Unit =
    super.afterEach()

  "AuthenticationService" should {
      "resolve an auth code when user exist" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.createUser(CreateUser("name@domain.com", Role.User, Instant.now(), Github, 1, None))

        // expect
        authenticationApplication.resolveAuthCode("authCode").right.value shouldBe Session(
          userId = 1,
          email = "name@domain.com",
          provider = Github,
          role = Role.User
        )
      }

      "resolve an auth code when user exists with administrator role" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.createUser(CreateUser("name@domain.com", Role.Administrator, Instant.now(), Github, 1, None))

        // expect
        authenticationApplication.resolveAuthCode("authCode").right.value shouldBe Session(
          userId = 1,
          email = "name@domain.com",
          provider = Github,
          role = Role.Administrator
        )
      }

      "resolve an auth code when user doesn't exist" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.getUser(1) should be(None)

        // expect
        authenticationApplication.resolveAuthCode("authCode").right.value shouldBe Session(
          userId = 1,
          email = "name@domain.com",
          provider = Github,
          role = Role.User
        )
      }

      "create an user when the user with a given email doesn't exist and generate keys and usage for him" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.getUser(1) should be(None)

        // when
        authenticationApplication.resolveAuthCode("authCode")

        // then
        userRepository.getUser(1).value should have(
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
    }
}
