package tech.cryptonomic.nautilus.cloud

import java.net.HttpURLConnection.{HTTP_FORBIDDEN, HTTP_NO_CONTENT, HTTP_OK}

import com.softwaremill.sttp._
import org.scalatest.{EitherValues, Matchers, OptionValues, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, Role, UpdateUser}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{InMemoryDatabase, WireMockServer}

class NautilusCloudStarterE2ETest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with InMemoryDatabase
    with WireMockServer {

  implicit val sttpBackend = HttpURLConnectionBackend()

  val nautilusContext = NautilusContext

  NautilusCloud.main(Array.empty)

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
        val response = sttp.get(uri"http://localhost:1235/users/1").send()

        // then
        response.code shouldBe HTTP_FORBIDDEN
      }
    }

  "current_login endpoint" should {

      "return HTTP 403 FORBIDDEN when user is not logged-in" in {
        // when
        val request = sttp.get(uri"http://localhost:1235/current_login").send()

        // then
        request.code shouldBe HTTP_FORBIDDEN
      }

      "should return email address and current role (user is default)" in {
        // given
        stubAuthServiceFor(authCode = "auth-code", email = "name@domain.com")
        val authCodeResult =
          sttp.get(uri"http://localhost:1235/github-callback?code=auth-code").followRedirects(false).send()

        // when
        val response = sttp.get(uri"http://localhost:1235/current_login").cookies(authCodeResult.cookies).send()

        // and
        response.code shouldBe HTTP_OK
        response.body.right.value shouldBe "{\"email\": \"name@domain.com\", \"role\": \"User\"}"
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
