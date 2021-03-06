package tech.cryptonomic.nautilus.cloud

import java.net.HttpURLConnection.{HTTP_FORBIDDEN, HTTP_NO_CONTENT, HTTP_OK}

import cats.implicits._
import com.softwaremill.sttp._
import io.circe.HCursor
import io.circe.parser._
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.user.{AdminUpdateUser, Role}
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

        // create user through first login
        login(email = "name@domain.com")

        // update role for that user
        nautilusContext.userApplication
          .updateUser(1, AdminUpdateUser(Role.Administrator.some))(adminSession)
          .unsafeRunSync()

        // log-in again with administrator role
        val authCodeResult =
          sttp
            .post(uri"http://localhost:1235/users/github-init")
            .header("Content-Type", "application/json")
            .body("""{"code": "auth-code"}""")
            .followRedirects(false)
            .send()
        val headers = authCodeResult.headers.toMap

        // when
        val response = sttp
          .get(uri"http://localhost:1235/users/1")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
          .send()

        // then
        response.code shouldBe HTTP_OK
        response.body.right.value should include("name@domain.com")
      }

      "return HTTP 403 FORBIDDEN when user is logged-in with user role (which is default)" in {
        // given
        val headers = login().headers.toMap

        // when
        val response = sttp
          .get(uri"http://localhost:1235/users/1")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
          .send()

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
        val log = login("name@domain.com")
        val headers = log.headers.toMap

        // when
        val response = sttp
          .get(uri"http://localhost:1235/users/me")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
          .send()

        // and
        response.code shouldBe HTTP_OK
        response.body.right.value should matchJson("""{"userEmail": "name@domain.com", "userRole": "user"}""")
      }

      "return HTTP 403 when ToS is not accepted" in {
        // given
        val headers = loginWithoutToS("name@domain.com").headers.toMap

        // when
        val response = sttp
          .get(uri"http://localhost:1235/users/me")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
          .send()

        // and
        response.code shouldBe HTTP_FORBIDDEN
      }

      "return user apiKeys generated with first login" in {
        // given
        val headers = login().headers.toMap

        // when
        val apiKeys = sttp
          .get(uri"http://localhost:1235/users/me/apiKeys")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
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

      "return HTTP 403 when ToS was not accepted" in {
        // given
        val headers = loginWithoutToS().headers.toMap

        // when
        val apiKeys = sttp
          .get(uri"http://localhost:1235/users/me/apiKeys")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
          .send()

        // then
        apiKeys.code shouldBe HTTP_FORBIDDEN
      }

      "refresh apiKeys" in {
        // given
        val headers = login().headers.toMap

        sttp
          .get(uri"http://localhost:1235/users/me/apiKeys")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
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
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
          .send()

        // then
        apiKeys.body.right.value should matchJson("""{
            |  "key": "exampleApiKey2",
            |  "environment": "prod"
            |}""".stripMargin)

        sttp
          .get(uri"http://localhost:1235/users/me/apiKeys")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
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
        val headers = login().headers.toMap

        // when
        val usageLeft = sttp
          .get(uri"http://localhost:1235/users/me/usage")
          .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
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

  "login endpoints" should {

      "set an authorization header after successful log-in" in {
        // when
        val response = login("name@domain.com")

        // then
        response.body.right.value should matchJson(
          """{"header":{"payloadType":"REGISTERED"},"payload":{"userId":1,"userEmail":"name@domain.com","userRole":"user"}}"""
        )
        response.headers.toMap.get("Set-Authorization").value should endWith(
          "xprovider~github&email~name%40domain.com&role~user&tosAccepted~true&id~1"
        )
      }

      "set an authorization header after successful log-in when user exists" in {
        // given
        login("name@domain.com")

        // when
        val response = sttp
          .post(uri"http://localhost:1235/users/github-init")
          .header("Content-Type", "application/json")
          .body("""{"code": "auth-code"}""")
          .followRedirects(false)
          .send()

        // then
        response.body.right.value should matchJson(
          """{"header":{"payloadType":"REGISTERED"},"payload":{"userId":1,"userEmail":"name@domain.com","userRole":"user"}}"""
        )
        response.headers.toMap.get("Set-Authorization").value should endWith(
          "xprovider~github&email~name%40domain.com&role~user&tosAccepted~true&id~1"
        )
      }
    }

  "logout endpoint" should {

      "invalidate session" in {
        // given
        val headers = login().headers.toMap

        // when
        val response =
          sttp
            .post(uri"http://localhost:1235/logout")
            .header("Authorization", headers.getOrElse("Set-Authorization", "not-defined"))
            .followRedirects(false)
            .send()

        // and
        response.code shouldBe HTTP_NO_CONTENT
        response.headers.toMap.get("Set-Authorization").value shouldBe ""

      }
    }

  private def login(email: String = "some@domain.com"): Response[String] = {
    stubAuthServiceFor(authCode = "auth-code", email = email)

    val githubInit = sttp
      .post(uri"http://localhost:1235/users/github-init")
      .header("Content-Type", "application/json")
      .body(s"""{"code": "auth-code"}""")
      .followRedirects(false)
      .send()

    val registrationAttemptId = extractRegistrationAttemptId(githubInit)

    val result = sttp
      .post(uri"http://localhost:1235/users/register")
      .header("Content-Type", "application/json")
      .body(s"""{
              |  "registrationAttemptId": "$registrationAttemptId",
              |  "tosAccepted": true,
              |  "newsletterAccepted": true
              |}""".stripMargin)
      .followRedirects(false)
      .send()

    result
  }

  private def loginWithoutToS(email: String = "some@domain.com"): Response[String] = {
    stubAuthServiceFor(authCode = "auth-code", email = email)

    val githubInit = sttp
      .post(uri"http://localhost:1235/users/github-init")
      .header("Content-Type", "application/json")
      .body(s"""{"code": "auth-code"}""")
      .followRedirects(false)
      .send()

    val registrationAttemptId = extractRegistrationAttemptId(githubInit)

    val result = sttp
      .post(uri"http://localhost:1235/users/accept-registration")
      .header("Content-Type", "application/json")
      .body(s"""{
               |  "registrationAttemptId": "$registrationAttemptId",
               |  "tosAccepted": false,
               |  "newsletterAccepted": true
               |}""".stripMargin)
      .followRedirects(false)
      .send()

    result
  }

  private def extractRegistrationAttemptId(githubInit: Response[String]): String =
    parse(githubInit.body.right.value).right.value.hcursor
      .get[HCursor]("payload")
      .right
      .value
      .value
      .hcursor
      .get[String]("registrationAttemptId")
      .right
      .get
}
