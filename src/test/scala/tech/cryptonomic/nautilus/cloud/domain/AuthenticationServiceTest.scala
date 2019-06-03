package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Id
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, OptionValues, WordSpec}
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class AuthenticationServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with MockFactory
    with BeforeAndAfterEach {

  val authRepositoryStub = new InMemoryAuthenticationProviderRepository(
    List(("authCode", "accessToken", "name@domain.com"))
  )
  val userRepository = new InMemoryUserRepository()

  val authenticationService =
    new AuthenticationService[Id](NautilusContext.authConfig, authRepositoryStub, userRepository)

  override protected def afterEach(): Unit = {
    super.afterEach()
    userRepository.clear()
  }

  "AuthenticationService" should {
      "resolve an auth code when user exists" in {
        // given
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
        userRepository.createUser(CreateUser("name@domain.com", Role.Administrator, Instant.now(), Github, None))

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.Administrator
        )
      }

      "resolve an auth code when user doesn't exist" in {
        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.User
        )
      }

      "create an user when user with a given email doesn't exist" in {
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
    }
}
