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
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, FixedClock}

class AuthenticationServiceTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with Fixtures
    with BeforeAndAfterEach {

  val authRepository = new InMemoryAuthenticationProviderRepository()
  val userRepository = new InMemoryUserRepository()
  val apiKeyRepository = new InMemoryApiKeyRepository()
  val tiersRepository = new InMemoryTierRepository()
  val resourcesRespository = new InMemoryResourceRepository()
  val now = ZonedDateTime.parse("2019-05-27T12:03:48.081+01:00").toInstant
  val clock = new FixedClock[Id](now)
  val apiKeyGenerator = new ApiKeyGenerator

  val authenticationService =
    new AuthenticationService[Id](
      DefaultNautilusContext.authConfig,
      authRepository,
      userRepository,
      new ApiKeyService(apiKeyRepository, resourcesRespository, tiersRepository, clock, apiKeyGenerator)
    )

  override protected def afterEach(): Unit = {
    super.afterEach()
    authRepository.clear()
    userRepository.clear()
    apiKeyRepository.clear()
    tiersRepository.clear()
  }

  "AuthenticationService" should {
      "resolve an auth code when user existdos" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.createUser(CreateUser("name@domain.com", Role.User, Instant.now(), Github, None))

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.User
        )
      }

      "resolve an auth code when user exists with administrator role" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        userRepository.createUser(CreateUser("name@domain.com", Role.Administrator, Instant.now(), Github, None))

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.Administrator
        )
      }

      "resolve an auth code when user doesn't exist" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        tiersRepository.create(TierName("shared", "free"), TierConfiguration("free tier", Usage(100, 1000), 10, Instant.now))
        userRepository.getUser(1) should be(None)

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.User
        )
      }

      "create an user when the user with a given email doesn't exist and generate keys and usage for him" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
        tiersRepository.create(TierName("shared", "free"), TierConfiguration("free tier", Usage(100, 1000), 10, Instant.now))
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
