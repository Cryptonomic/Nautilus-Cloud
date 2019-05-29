package security

import cats.Id
import com.github.tomakehurst.wiremock.WireMockServer
import com.softwaremill.sttp.HttpURLConnectionBackend
import fixtures.Fixtures
import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import org.scalamock.scalatest.MockFactory
import tech.cryptonomic.cloud.nautilus.security.{AuthProviderConfig, OauthRepository, OauthService}

import scala.language.postfixOps

class OauthServiceTest extends WordSpec with Matchers with Fixtures with EitherValues with MockFactory with BeforeAndAfterEach {

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

  val oauthRepositoryStub = stub[OauthRepository[Id]]
  val oauthService = new OauthService[Id](config, oauthRepositoryStub)

  "OauthService" should {
      "return proper login url" in {
        oauthService.loginUrl shouldBe "http://localhost:8089/login/oauth/authorize?scope=user:email&client_id=clientId"
      }

      "resolve an auth code" in {
        // given
        (oauthRepositoryStub.exchangeCodeForAccessToken _).when("authCode").returns(Right("accessToken"))
        (oauthRepositoryStub.fetchEmail _).when("accessToken").returns(Right("name@domain.com"))

        // expect
        oauthService.resolveAuthCode("authCode").right.value shouldBe "name@domain.com"
      }
    }
}
