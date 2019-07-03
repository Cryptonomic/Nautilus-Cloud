package tech.cryptonomic.nautilus.cloud.adapters.authentication.github

import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.tools.DefaultNautilusContext

import scala.language.postfixOps

class GithubAuthenticationConfigurationTest extends WordSpec with Matchers {

  "GithubAuthenticationConfiguration" should {

      val config = DefaultNautilusContext.githubConfig.copy(
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
