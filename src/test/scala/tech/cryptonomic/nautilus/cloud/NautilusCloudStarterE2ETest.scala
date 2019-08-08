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

  override def beforeEach(): Unit = {
    super.beforeEach()
    nautilusContext.apiKeyGenerator.asInstanceOf[FixedApiKeyGenerator].reset
    applySchemaWithFixtures()
  }

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
        sttp.get(uri"http://localhost:1235/users/github-init?code=auth-code").followRedirects(false).send()

        // update role for that user
        nautilusContext.userRepository
          .updateUser(1, UpdateUser(Role.Administrator, None))
          .unsafeRunSync()

        // log-in again with administrator role
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/users/github-init?code=auth-code").followRedirects(false).send()

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
          sttp.get(uri"http://localhost:1235/users/github-init?code=auth-code").followRedirects(false).send()

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
          sttp.get(uri"http://localhost:1235/users/github-init?code=auth-code").followRedirects(false).send()

        // when
        val response = sttp.get(uri"http://localhost:1235/users/me").cookies(authCodeResult.cookies).send()

        // and
        response.code shouldBe HTTP_OK
        response.body.right.value should matchJson("""{"userEmail": "name@domain.com", "userRole": "user"}""")
      }

      "return user apiKeys generated with first login" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")

        val authCodeResult =
          sttp
            .get(uri"http://localhost:1235/users/github-init?code=auth-code")
            .followRedirects(false)
            .send()

        // when
        val apiKeys = sttp
          .get(uri"http://localhost:1235/users/me/apiKeys")
          .cookies(authCodeResult.cookies)
          .send()

        // then
        apiKeys.code shouldBe HTTP_OK
        apiKeys.body.right.value should matchJson("""[
                                             |  {
                                             |    "keyId": 1,
                                             |    "key": "exampleApiKey0",
                                             |    "environment": "prod",
                                             |    "userId": 1
                                             |  },
                                             |  {
                                             |    "keyId": 2,
                                             |    "key": "exampleApiKey1",
                                             |    "environment": "dev",
                                             |    "userId": 1
                                             |  }
                                             |]""".stripMargin)
      }

      "refresh apiKeys" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")

        val authCodeResult =
          sttp
            .get(uri"http://localhost:1235/users/github-init?code=auth-code")
            .followRedirects(false)
            .send()

        sttp
          .get(uri"http://localhost:1235/users/me/apiKeys")
          .cookies(authCodeResult.cookies)
          .send()
          .body
          .right
          .value should matchJson("""[
                                            |  {
                                            |    "key": "exampleApiKey0",
                                            |    "environment": "prod"
                                            |  },
                                            |  {
                                            |    "key": "exampleApiKey1",
                                            |    "environment": "dev"
                                            |  }
                                            |]""".stripMargin)

        // when
        val apiKeys = sttp
          .post(uri"http://localhost:1235/users/me/apiKeys/prod/refresh")
          .cookies(authCodeResult.cookies)
          .send()

        // then
        sttp
          .get(uri"http://localhost:1235/users/me/apiKeys")
          .cookies(authCodeResult.cookies)
          .send()
          .body
          .right
          .value should matchJson("""[
                                    |  {
                                    |    "key": "exampleApiKey2",
                                    |    "environment": "prod"
                                    |  },
                                    |  {
                                    |    "key": "exampleApiKey1",
                                    |    "environment": "dev"
                                    |  }
                                    |]""".stripMargin)
      }

      "return initial usageLeft generated for a user with first login" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")

        val authCodeResult =
          sttp
            .get(uri"http://localhost:1235/users/github-init?code=auth-code")
            .followRedirects(false)
            .send()

        // when
        val usageLeft = sttp
          .get(uri"http://localhost:1235/users/me/usage")
          .cookies(authCodeResult.cookies)
          .send()

        // then
        usageLeft.code shouldBe HTTP_OK
        usageLeft.body.right.value should matchJson("""[
                                                    |  {
                                                    |    "key": "exampleApiKey0",
                                                    |    "usage": {
                                                    |      "daily": 0,
                                                    |      "monthly": 0
                                                    |    }
                                                    |  },
                                                    |  {
                                                    |    "key": "exampleApiKey1",
                                                    |    "usage": {
                                                    |      "daily": 0,
                                                    |      "monthly": 0
                                                    |    }
                                                    |  }
                                                    |]""".stripMargin)
      }
    }

  "users/github-init endpoint" should {

      "set a cookie after successfull log-in" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")

        // when
        val response =
          sttp.get(uri"http://localhost:1235/users/github-init?code=auth-code").followRedirects(false).send()

        // then
        response.isRedirect shouldBe true
        response.cookies.headOption.value.name shouldBe "_sessiondata" // check if auth cookie named "_sessiondata" was set up
      }
    }

  "logout endpoint" should {

      "invalidate session" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/users/github-init?code=auth-code").followRedirects(false).send()

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
