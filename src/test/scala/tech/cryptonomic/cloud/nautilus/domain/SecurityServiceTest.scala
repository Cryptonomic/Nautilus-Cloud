package tech.cryptonomic.cloud.nautilus.domain

import java.time.Instant

import cats.Id
import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.sttp.HttpURLConnectionBackend
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.adapters.sttp.GithubConfig
import tech.cryptonomic.cloud.nautilus.domain.security.{GithubRepository, Session}
import tech.cryptonomic.cloud.nautilus.domain.user.{AuthenticationProvider, CreateUser, Role, User, UserRepository}
import tech.cryptonomic.cloud.nautilus.fixtures.Fixtures

import scala.concurrent.duration._
import scala.language.postfixOps

class SecurityServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with MockFactory {

  val port = 8089
  val host = "localhost"

  val wireMockServer = new WireMockServer(port)

  implicit val sttpBackend = HttpURLConnectionBackend()

  private val config = GithubConfig(
    clientId = "clientId",
    clientSecret = "clientSecret",
    accessTokenUrl = "http://localhost:8089/login/oauth/access_token",
    loginUrl = "http://localhost:8089/login/oauth/authorize",
    getEmailsUrl = "http://localhost:8089/user",
    connectionTimeout = 100 milliseconds,
    readTimeout = 100 milliseconds
  )

  val oauthRepositoryStub = stub[GithubRepository[Id]]
  val userRepositoryStub = stub[UserRepository[Id]]
  val oauthService = new SecurityService[Id](config, oauthRepositoryStub, userRepositoryStub)

  "OauthService" should {
      "return proper login url" in {
        oauthService.loginUrl shouldBe "http://localhost:8089/login/oauth/authorize?scope=user:email&client_id=clientId"
      }

      "resolve an auth code when user exists" in {
        // given
        (oauthRepositoryStub.exchangeCodeForAccessToken _).when("authCode").returns(Right("accessToken"))
        (oauthRepositoryStub.fetchEmail _)
          .when("accessToken")
          .returns(Right("name@domain.com"))
        (userRepositoryStub.getUserByEmailAddress _).when("name@domain.com").returns(Some(User(1, "name@domain.com", Role.User, Instant.now(), AuthenticationProvider.Github, None)))

        // expect
        oauthService.resolveAuthCode("authCode").right.value shouldBe Session("name@domain.com", AuthenticationProvider.Github, Role.User)
      }

    "resolve an auth code when user doesn't exist" in {
        // given
        (oauthRepositoryStub.exchangeCodeForAccessToken _).when("authCode").returns(Right("accessToken"))
        (oauthRepositoryStub.fetchEmail _)
          .when("accessToken")
          .returns(Right("name@domain.com"))
        (userRepositoryStub.getUserByEmailAddress _).when("name@domain.com").returns(None)
        (userRepositoryStub.createUser _).when(CreateUser("name@domain.com", Role.User, Instant.now(), AuthenticationProvider.Github, None)).returns(Right(1))

        // expect
        oauthService.resolveAuthCode("authCode").right.value shouldBe Session("name@domain.com", AuthenticationProvider.Github, Role.User)
      }
    }
}
