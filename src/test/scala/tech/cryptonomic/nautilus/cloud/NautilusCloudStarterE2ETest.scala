package tech.cryptonomic.nautilus.cloud

import java.net.HttpURLConnection.{HTTP_FORBIDDEN, HTTP_NO_CONTENT, HTTP_OK}

import com.softwaremill.sttp._
import org.scalatest.{EitherValues, Matchers, OptionValues, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.user.{Role, UpdateUser}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{InMemoryDatabase, NautilusTestRunner, WireMockServer}
import tech.cryptonomic.nautilus.cloud.tools.JsonMatchers
import tech.cryptonomic.nautilus.cloud.tools.{InMemoryDatabase, WireMockServer}

class NautilusCloudStarterE2ETest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with InMemoryDatabase
    with NautilusTestRunner
    with JsonMatchers
    with WireMockServer {

  implicit val sttpBackend = HttpURLConnectionBackend()

  val nautilusContext = NautilusContext

  "users API" should {

      "return HTTP 403 FORBIDDEN when user is not logged-in" in {
        // when
        val response = sttp.get(uri"http://localhost:1235/users/1").send()

        // then
        response.code shouldBe HTTP_FORBIDDEN
      }

      "return info about user when user is logged-in with administrator role" in {
        // given
        nautilusContext.tierRepository.createDefaultTier.unsafeRunSync()
        nautilusContext.resourcesRepository.createDefaultResources.unsafeRunSync()
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")

        // create user through first login
        sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // update role for that user
        nautilusContext.userRepository
          .updateUser(1, UpdateUser(Role.Administrator, None))
          .unsafeRunSync()

        // log-in again with administrator role
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // when
        val response = sttp.get(uri"http://localhost:1235/users/1").cookies(authCodeResult.cookies).send()

        // then
        response.code shouldBe HTTP_OK
        response.body.right.value should include("name@domain.com")

        // get user API Keys
        val apiKeys = sttp.get(uri"http://localhost:1235/users/1/apiKeys").cookies(authCodeResult.cookies).send()

        val userApiKeys = nautilusContext.apiKeysRepository.getUserApiKeys(1).unsafeRunSync()
        userApiKeys.size shouldBe 2
        val exampleKey  = userApiKeys.map(_.key).head

        // then
        apiKeys.code shouldBe HTTP_OK
        apiKeys.body.right.value should include(exampleKey)

        // get usage left for the API keys
        val usageLeft = sttp.get(uri"http://localhost:1235/users/1/usage").cookies(authCodeResult.cookies).send()

        // then
        usageLeft.code shouldBe HTTP_OK
        usageLeft.body.right.value should include(exampleKey)
      }

      "return HTTP 403 FORBIDDEN when user is logged-in with user role (which is default)" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // when
        val response = sttp.get(uri"http://localhost:1235/users/1").cookies(authCodeResult.cookies).send()

        // then
        response.code shouldBe HTTP_FORBIDDEN
      }
    }

  "get current user endpoint" should {

      "return HTTP 403 FORBIDDEN when user is not logged-in" in {
        // when
        val request = sttp.get(uri"http://localhost:1235/users/me").send()

        // then
        request.code shouldBe HTTP_FORBIDDEN
      }

      "should return email address and current role (user is default)" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // when
        val response = sttp.get(uri"http://localhost:1235/users/me").cookies(authCodeResult.cookies).send()

        // and
        response.code shouldBe HTTP_OK
        response.body.right.value should matchJson("""{"userEmail": "name@domain.com", "userRole": "user"}""")
      }
    }

  "github-callback endpoint" should {

      "set a cookie after successfull log-in" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")

        // when
        val response =
          sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // then
        response.isRedirect shouldBe true
        response.cookies.headOption.value.name shouldBe "_sessiondata" // check if auth cookie named "_sessiondata" was set up
      }

      "logout endpoint should invalidate session" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // when
        val response =
          sttp.post(uri"http://localhost:1235/logout").cookies(authCodeResult.cookies).followRedirects(false).send()

        // and
        response.code shouldBe HTTP_NO_CONTENT
        response.cookies.headOption.value should have(
          'name ("_sessiondata"),
          'value ("deleted")
        )
      }
    }
}
