package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.sttp.HttpURLConnectionBackend
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.GithubConfig
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AuthenticationConfiguration, AuthenticationProviderRepository}
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

  val port = 8089
  val host = "localhost"

  val wireMockServer = new WireMockServer(port)

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

  val oauthRepositoryStub = stub[AuthenticationProviderRepository[Id]]
  val oauthConfig = stub[AuthenticationConfiguration]
  val authenticationService = new AuthenticationService[Id](oauthConfig, oauthRepositoryStub)

  "AuthenticationService" should {
      "resolve an auth code" in {
        // given
        (oauthRepositoryStub.exchangeCodeForAccessToken _).when("authCode").returns(Right("accessToken"))
        (oauthRepositoryStub.fetchEmail _)
          .when("accessToken")
          .returns(Right("name@tech.cryptonomic.cloud.nautilus.domain.com"))

        // expect
        authenticationService
          .resolveAuthCode("authCode")
          .right
          .value shouldBe "name@tech.cryptonomic.cloud.nautilus.domain.com"
      }
    }
}
