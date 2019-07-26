package tech.cryptonomic.nautilus.cloud.domain

import java.time.{Instant, ZonedDateTime}

import cats.Id
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.inmemory._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyGenerator
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.{TierConfiguration, TierName, Usage}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, FixedClock, IdContext}

class AuthenticationServiceTest
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
  val authenticationService = context.authenticationService
  val authRepository = context.authRepository
  val userRepository = context.userRepository
  val tiersRepository = context.tiersRepository
  val apiKeyRepository = context.apiKeyRepository

  override protected def afterEach(): Unit =
    super.afterEach()

  "AuthenticationService" should {
      "resolve an auth code when user existdos" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.createUser(CreateUser("name@domain.com", Role.User, Instant.now(), Github, 1, None))

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          id = 1,
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
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          id = 1,
          email = "name@domain.com",
          provider = Github,
          role = Role.Administrator
        )
      }

      "resolve an auth code when user doesn't exist" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        tiersRepository.create(
          TierName("shared", "free"),
          TierConfiguration("free tier", Usage(100, 1000), 10, Instant.now)
        )
        userRepository.getUser(1) should be(None)

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          id = 1,
          email = "name@domain.com",
          provider = Github,
          role = Role.User
        )
      }

      "create an user when the user with a given email doesn't exist and generate keys and usage for him" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        tiersRepository.create(
          TierName("shared", "free"),
          TierConfiguration("free tier", Usage(100, 1000), 10, Instant.now)
        )
        userRepository.getUser(1) should be(None)

        // when
        authenticationService.resolveAuthCode("authCode")

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
        authenticationService.resolveAuthCode("authCode").left.value
      }
    }
}
