package security

import cats.Id
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.softwaremill.sttp.HttpURLConnectionBackend
import fixtures.Fixtures
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.security.{AuthProviderConfig, SttpOauthRepository, SttpOauthServiceException}

import scala.language.postfixOps

class SttpOauthRepositoryTest extends WordSpec with Matchers with Fixtures with EitherValues with BeforeAndAfterEach {

  val port = 8089
  val host = "localhost"

  val wireMockServer = new WireMockServer(port)

  private val config = AuthProviderConfig(
    "clientId",
    "clientSecret",
    "http://localhost:8089/login/oauth/access_token",
    "http://localhost:8089/login/oauth/authorize",
    "http://localhost:8089/user/emails",
    100,
    100
  )
  implicit val sttpBackend = HttpURLConnectionBackend()
  val oauthRepository = new SttpOauthRepository[Id](config)

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(host, port)
    wireMockServer.resetAll()
  }

  "SttpOauthRepository" should {

      // given
      "exchange code for an access token" in {
        stubFor(
          post(urlEqualTo("/login/oauth/access_token"))
            .withRequestBody(equalTo("client_id=clientId&client_secret=clientSecret&code=authCode"))
            .willReturn(
              aResponse()
                .withBody("""{"access_token": "stubbed-access-token"}""")
            )
        )

        // expect
        oauthRepository.exchangeCodeForAccessToken("authCode").right.value shouldBe "stubbed-access-token"
      }

      // given
      "return a SttpOauthServiceException when oauth server is not accessible when fetching access token" in {
        //when
        wireMockServer.stop()

        // when
        val result = oauthRepository.exchangeCodeForAccessToken("authCode")

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
        val result = oauthRepository.exchangeCodeForAccessToken("authCode")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      "return a SttpOauthServiceException when oauth server returns other response code when fetching access token" in {
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
        val result = oauthRepository.exchangeCodeForAccessToken("authCode")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      // given
      "fetch email" in {
        stubFor(
          get(urlEqualTo("/user/emails"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withBody("""[
                            |    {
                            |        "email": "dorian.sarnowski@gmail.com",
                            |        "primary": true,
                            |        "verified": true,
                            |        "visibility": "public"
                            |    }
                            |]""".stripMargin)
            )
        )

        // expect
        oauthRepository.fetchEmail("stubbed-access-token").right.value shouldBe "dorian.sarnowski@gmail.com"
      }

      // given
      "return a SttpOauthServiceException when oauth server is not accessible when fetching email" in {
        //when
        wireMockServer.stop()

        // when
        val result = oauthRepository.fetchEmail("access-token")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      "return a SttpOauthServiceException when request for email times out" in {
        // given
        stubFor(
          get(urlEqualTo("/user/emails"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withBody("""[
                            |    {
                            |        "email": "dorian.sarnowski@gmail.com",
                            |        "primary": true,
                            |        "verified": true,
                            |        "visibility": "public"
                            |    }
                            |]""".stripMargin)
                .withFixedDelay(10000)
            )
        )

        // when
        val result = oauthRepository.fetchEmail("stubbed-access-token")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      "return a SttpOauthServiceException when oauth server returns other response code when fetching email" in {
        // given
        stubFor(
          get(urlEqualTo("/user/emails"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withStatus(503)
            )
        )

        // when
        val result = oauthRepository.fetchEmail("stubbed-access-token")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }

      "return a SttpOauthServiceException when oauth server returns no valid email" in {
        // given
        stubFor(
          get(urlEqualTo("/user/emails"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withBody("""[
                            |    {
                            |        "email": "dorian.sarnowski@gmail.com",
                            |        "primary": true,
                            |        "verified": false,
                            |        "visibility": "public"
                            |    }
                            |]""".stripMargin)
            )
        )

        // when
        val result = oauthRepository.fetchEmail("stubbed-access-token")

        // then
        result.left.value shouldBe a[SttpOauthServiceException]
      }
    }
}
