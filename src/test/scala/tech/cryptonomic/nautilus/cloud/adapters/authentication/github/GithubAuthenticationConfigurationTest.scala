package tech.cryptonomic.nautilus.cloud.adapters.authentication.github

import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

import scala.concurrent.duration._
import scala.language.postfixOps

class GithubAuthenticationConfigurationTest extends WordSpec with Matchers with Fixtures {

  "GithubAuthenticationConfiguration" should {

      val config = githubConfig.copy(
        clientId = "clientId",
        loginUrl = "http://localhost:8089/login/oauth/authorize"
      )

      "prepare login url" in {
        // given
        val sut = GithubAuthenticationConfiguration(config)

        // then
        sut.loginUrl shouldBe "http://localhost:8089/login/oauth/authorize?scope=user:email&client_id=clientId"
      }
    }
}
