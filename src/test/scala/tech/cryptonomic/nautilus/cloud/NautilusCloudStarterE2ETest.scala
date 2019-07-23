package tech.cryptonomic.nautilus.cloud

import java.net.HttpURLConnection.{HTTP_FORBIDDEN, HTTP_NO_CONTENT, HTTP_OK}

import com.softwaremill.sttp._
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.user.{Role, UpdateUser}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools._

class NautilusCloudStarterE2ETest
    extends WordSpec
    with NautilusE2EContext
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with InMemoryDatabase
    with JsonMatchers
    with WireMockServer
    with NautilusTestRunner {

  implicit val sttpBackend = HttpURLConnectionBackend()

  "users API" should {

      "return HTTP 403 FORBIDDEN when user is not logged-in" in {
        // when
        val response = sttp.get(uri"http://localhost:1235/users/1").send()

        // then
        response.code shouldBe HTTP_FORBIDDEN
      }

      "return info about user when user is logged-in with administrator role" in {
        // given
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
        createDefaultTier(nautilusContext.tierRepository).unsafeRunSync()
        createDefaultResources(nautilusContext.resourcesRepository).unsafeRunSync()
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // when
        val response = sttp.get(uri"http://localhost:1235/users/me").cookies(authCodeResult.cookies).send()

        // and
        response.code shouldBe HTTP_OK
        response.body.right.value should matchJson("""{"userEmail": "name@domain.com", "userRole": "user"}""")
      }
  }

  "sth" should {
    "return user apiKeys and usageLeft generated with first login" in {
      // given
      createDefaultTier(nautilusContext.tierRepository).unsafeRunSync()
      createDefaultResources(nautilusContext.resourcesRepository)
        .unsafeRunSync()
      stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")

      val authCodeResult =
        sttp
          .get(uri"http://localhost:1235/github-callback?code=auth-code")
          .followRedirects(false)
          .send()

      // when
      val apiKeys = sttp
        .get(uri"http://localhost:1235/users/me/apiKeys")
        .cookies(authCodeResult.cookies)
        .send()

      // then
      val exampleKey = "exampleApiKey"
      apiKeys.code shouldBe HTTP_OK
      apiKeys.body.right.value should include(exampleKey)

      // when
      val usageLeft = sttp
        .get(uri"http://localhost:1235/users/me/usage")
        .cookies(authCodeResult.cookies)
        .send()

      // then
      usageLeft.code shouldBe HTTP_OK
      usageLeft.body.right.value should include(exampleKey)
    }
  }

  "github-callback endpoint" should {

      "set a cookie after successfull log-in" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")
        createDefaultTier(nautilusContext.tierRepository).unsafeRunSync()
        createDefaultResources(nautilusContext.resourcesRepository).unsafeRunSync()

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
        createDefaultTier(nautilusContext.tierRepository).unsafeRunSync()
        createDefaultResources(nautilusContext.resourcesRepository).unsafeRunSync()
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
