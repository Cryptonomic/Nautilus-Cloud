package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Id
import com.softwaremill.sttp.HttpURLConnectionBackend
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.{GithubAuthenticationConfiguration, GithubConfig}
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AuthenticationProviderRepository, Session}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, User, UserRepository}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

import scala.concurrent.duration._
import scala.language.postfixOps

class AuthenticationServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with MockFactory
    with BeforeAndAfterEach {

  implicit val sttpBackend = HttpURLConnectionBackend()

  private val config = GithubConfig(
    clientId = "clientId",
    clientSecret = "clientSecret",
    accessTokenUrl = "http://localhost:8089/login/oauth/access_token",
    loginUrl = "http://localhost:8089/login/oauth/authorize",
    emailsUrl = "http://localhost:8089/user",
    connectionTimeout = 100 milliseconds,
    readTimeout = 100 milliseconds
  )

  val authRepositoryStub = stub[AuthenticationProviderRepository[Id]]
  val authConfig = GithubAuthenticationConfiguration(config)
  val userRepositoryStub = stub[UserRepository[Id]]

  val authenticationService = new AuthenticationService[Id](authConfig, authRepositoryStub, userRepositoryStub)

  "AuthenticationService" should {
      "resolve an auth code when user exists" in {
        // given
        (authRepositoryStub.exchangeCodeForAccessToken _).when("authCode").returns(Right("accessToken"))
        (authRepositoryStub.fetchEmail _)
          .when("accessToken")
          .returns(Right("name@domain.com"))
        (userRepositoryStub.getUserByEmailAddress _)
          .when("name@domain.com")
          .returns(Some(User(1, "name@domain.com", Role.User, Instant.now(), Github, None)))

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.User
        )
      }

      "resolve an auth code when user doesn't exist" in {
        // given
        (authRepositoryStub.exchangeCodeForAccessToken _).when("authCode").returns(Right("accessToken"))
        (authRepositoryStub.fetchEmail _)
          .when("accessToken")
          .returns(Right("name@domain.com"))
        (userRepositoryStub.getUserByEmailAddress _).when("name@domain.com").returns(None)
        (userRepositoryStub.createUser _)
          .when(argThat((it: CreateUser) => it.userEmail == "name@domain.com" && it.userRole == Role.User))
          .returns(Right(1))

        // expect
        authenticationService.resolveAuthCode("authCode").right.value shouldBe Session(
          "name@domain.com",
          Github,
          Role.User
        )
      }
    }
}
