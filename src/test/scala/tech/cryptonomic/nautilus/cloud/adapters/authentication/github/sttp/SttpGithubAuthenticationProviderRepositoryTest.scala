package tech.cryptonomic.nautilus.cloud.adapters.authentication.github.sttp

import com.github.tomakehurst.wiremock.client.WireMock._
import com.softwaremill.sttp.HttpURLConnectionBackend
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, WireMockServer}

class SttpGithubAuthenticationProviderRepositoryTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with BeforeAndAfterEach
    with WireMockServer {

  val context = DefaultNautilusContext
  val authRepository = context.authRepository

  implicit val sttpBackend = HttpURLConnectionBackend()

  "SttpGithubAuthenticationProviderRepository" should {

      "exchange code for an access token" in {
        // given
        stubFor(
          post(urlEqualTo("/login/oauth/access_token"))
            .withRequestBody(equalTo("client_id=client-id&client_secret=client-secret&code=authCode"))
            .willReturn(
              aResponse()
                .withBody("""{"access_token": "stubbed-access-token"}""")
            )
        )

        // expect
        authRepository
          .exchangeCodeForAccessToken("authCode")
          .unsafeRunSync()
          .right
          .value shouldBe "stubbed-access-token"
      }

      "return a SttpOauthServiceException when oauth server returns other response code when fetching access token" in {
        // given
        stubFor(
          post(urlEqualTo("/login/oauth/access_token"))
            .withRequestBody(equalTo("client_id=client-id&client_secret=client-secret&code=authCode"))
            .willReturn(
              aResponse()
                .withStatus(503)
            )
        )

        // when
        val result = authRepository.exchangeCodeForAccessToken("authCode")

        // then
        result.unsafeRunSync().left.value shouldBe a[SttpGithubAuthenticationProviderException]
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
        authRepository.fetchEmail("stubbed-access-token").unsafeRunSync().right.value shouldBe "name@domain.com"
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
        val result = authRepository.fetchEmail("stubbed-access-token")

        // then
        result.unsafeRunSync().left.value shouldBe a[SttpGithubAuthenticationProviderException]
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
        val result = authRepository.fetchEmail("stubbed-access-token")

        // then
        result.unsafeRunSync().left.value shouldBe a[SttpGithubAuthenticationProviderException]
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
        val result = authRepository.fetchEmail("stubbed-access-token")

        // then
        result.unsafeRunSync().left.value shouldBe a[SttpGithubAuthenticationProviderException]
      }
    }
}
