package tech.cryptonomic.cloud.nautilus.domain

import cats.Id
import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.sttp.HttpURLConnectionBackend
import tech.cryptonomic.cloud.nautilus.fixtures.Fixtures
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.adapters.sttp.GithubConfig
import tech.cryptonomic.cloud.nautilus.domain.security.GithubRepository

import scala.language.postfixOps

class SecurityServiceTest extends WordSpec with Matchers with Fixtures with EitherValues with MockFactory with BeforeAndAfterEach {

  val port = 8089
  val host = "localhost"

  val wireMockServer = new WireMockServer(port)

  implicit val sttpBackend = HttpURLConnectionBackend()

  private val config = GithubConfig(
    "clientId",
    "clientSecret",
    "http://localhost:8089/login/oauth/access_token",
    "http://localhost:8089/login/oauth/authorize",
    "http://localhost:8089/user",
    100,
    100
  )

  val oauthRepositoryStub = stub[GithubRepository[Id]]
  val oauthService = new SecurityService[Id](config, oauthRepositoryStub)

  "OauthService" should {
      "return proper login url" in {
        oauthService.loginUrl shouldBe "http://localhost:8089/login/oauth/authorize?scope=user:email&client_id=clientId"
      }

      "resolve an auth code" in {
        // given
        (oauthRepositoryStub.exchangeCodeForAccessToken _).when("authCode").returns(Right("accessToken"))
        (oauthRepositoryStub.fetchEmail _).when("accessToken").returns(Right("name@tech.cryptonomic.cloud.nautilus.domain.com"))

        // expect
        oauthService.resolveAuthCode("authCode").right.value shouldBe "name@tech.cryptonomic.cloud.nautilus.domain.com"
      }
    }
}
