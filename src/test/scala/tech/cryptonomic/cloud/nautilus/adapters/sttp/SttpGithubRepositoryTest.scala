package tech.cryptonomic.cloud.nautilus.adapters.sttp

import cats.Id
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.softwaremill.sttp.HttpURLConnectionBackend
import tech.cryptonomic.cloud.nautilus.fixtures.Fixtures
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}

import scala.concurrent.duration._

import scala.language.postfixOps

class SttpGithubRepositoryTest extends WordSpec with Matchers with Fixtures with EitherValues with BeforeAndAfterEach {

  val port = 8089
  val host = "localhost"

  val wireMockServer = new WireMockServer(port)

  private val config = GithubConfig(
    clientId = "clientId",
    clientSecret = "clientSecret",
    accessTokenUrl = "http://localhost:8089/login/oauth/access_token",
    loginUrl = "http://localhost:8089/login/oauth/authorize",
    getEmailsUrl = "http://localhost:8089/user/emails",
    connectionTimeout = 100 milliseconds,
    readTimeout = 100 milliseconds
  )
  implicit val sttpBackend = HttpURLConnectionBackend()
  val oauthRepository = new SttpGithubRepository[Id](config)

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(host, port)
    wireMockServer.resetAll()
  }

  override def afterEach {
    wireMockServer.stop()
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
                            |        "email": "name@domain.com",
                            |        "primary": true,
                            |        "verified": true,
                            |        "visibility": "public"
                            |    }
                            |]""".stripMargin)
            )
        )

        // expect
        oauthRepository.fetchEmail("stubbed-access-token").right.value shouldBe "name@domain.com"
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
                            |        "email": "name@domain.com",
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

      "return a SttpOauthServiceException when oauth server returns no verified email" in {
        // given
        stubFor(
          get(urlEqualTo("/user/emails"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withBody("""[
                            |    {
                            |        "email": "name@domain.com",
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

      "return a SttpOauthServiceException when oauth server returns no primary email" in {
        // given
        stubFor(
          get(urlEqualTo("/user/emails"))
            .withHeader("Authorization", equalTo("Bearer stubbed-access-token"))
            .willReturn(
              aResponse()
                .withBody("""[
                            |    {
                            |        "email": "name@domain.com",
                            |        "primary": false,
                            |        "verified": true,
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
