package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Id
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, OptionValues, WordSpec}
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role}

class AuthenticationServiceTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach {

  val authRepository = new InMemoryAuthenticationProviderRepository()
  val userRepository = new InMemoryUserRepository()

  val authenticationService =
    new AuthenticationService[Id](NautilusContext.authConfig, authRepository, userRepository)

  override protected def afterEach(): Unit = {
    super.afterEach()
    authRepository.clear()
    userRepository.clear()
  }

  "AuthenticationService" should {
      "resolve an auth code when user exists" in {
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
        userRepository.getUser(1) should be(None)

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.User
        )
      }

      "create an user when the user with a given email doesn't exist" in {
        // given
        authRepository.addMapping("authCode", "accessToken", "name@domain.com")
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
      }

      "return Left when a given auth code shouldn't be resolved" in {
        // expect
        authenticationService.resolveAuthCode("authCode").left.value
      }
    }
}
