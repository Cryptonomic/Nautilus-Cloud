package security

import cats.Id
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.softwaremill.sttp.HttpURLConnectionBackend
import fixtures.Fixtures
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.security.{AuthProviderConfig, OauthService, SttpOauthRepository, SttpOauthServiceException}

import scala.language.postfixOps

class SttpOauthServiceTest extends WordSpec with Matchers with Fixtures with EitherValues with BeforeAndAfterEach {

  val port = 8089
  val host = "localhost"

  val wireMockServer = new WireMockServer(port)

  implicit val sttpBackend = HttpURLConnectionBackend()

  private val config = AuthProviderConfig(
    "clientId",
    "clientSecret",
    "http://localhost:8089/login/oauth/access_token",
    "http://localhost:8089/login/oauth/authorize",
    "http://localhost:8089/user",
    100,
    100
  )
  val oauthService = new OauthService[Id](config, new SttpOauthRepository[Id](config))

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(host, port)
    wireMockServer.resetAll()
  }

  "SttpOauthService" should {
      "return proper login url" in {
        oauthService.loginUrl shouldBe "http://localhost:8089/login/oauth/authorize?client_id=clientId"
      }

      // given
      "resolve auth code" in {
        stubFor(
          post(urlEqualTo("/login/oauth/access_token"))
            .withRequestBody(equalTo("client_id=clientId&client_secret=clientSecret&code=authCode"))
            .willReturn(
              aResponse()
                .withBody("""{"access_token": "stubbed-access-token"}""")
            )
        )
        stubFor(
          get(urlEqualTo("/user"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withBody("""{"email": "dorian.sarnowski@gmail.com"}""")
            )
        )

        // expect
        oauthService.resolveAuthCode("authCode").right.value shouldBe "dorian.sarnowski@gmail.com"
      }

      // given
      "return a SttpOauthServiceException when oauth server is not accessible" in {
        //when
        wireMockServer.stop()

        // when
        val result = oauthService.resolveAuthCode("authCode")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      "return a SttpOauthServiceException when request for access token times out" in {
        // given
        stubFor(
          post(urlEqualTo("/login/oauth/access_token"))
            .withRequestBody(equalTo("client_id=clientId&client_secret=clientSecret&code=authCode"))
            .willReturn(
              aResponse()
                .withBody("""{"access_token": "stubbed-access-token"}""")
                .withFixedDelay(100000)
            )
        )

        // when
        val result = oauthService.resolveAuthCode("authCode")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      "return a SttpOauthServiceException when request for user data times out" in {
        // given
        stubFor(
          post(urlEqualTo("/login/oauth/access_token"))
            .withRequestBody(equalTo("client_id=clientId&client_secret=clientSecret&code=authCode"))
            .willReturn(
              aResponse()
                .withBody("""{"access_token": "stubbed-access-token"}""")
            )
        )

        stubFor(
          get(urlEqualTo("/user"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withBody("""{"email": "dorian.sarnowski@gmail.com"}""")
                .withFixedDelay(100000)
            )
        )

        // when
        val result = oauthService.resolveAuthCode("authCode")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      "return a SttpOauthServiceException when oauth server returns other response code" in {
        // given
        stubFor(
          post(urlEqualTo("/login/oauth/access_token"))
            .withRequestBody(equalTo("client_id=clientId&client_secret=clientSecret&code=authCode"))
            .willReturn(
              aResponse()
                .withStatus(503)
            )
        )

        // when
        val result = oauthService.resolveAuthCode("authCode")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }
    }
}
