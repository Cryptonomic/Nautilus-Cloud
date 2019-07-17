package tech.cryptonomic.nautilus.cloud.adapters.akka

import java.time.ZonedDateTime

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.{InMemoryApiKeyRepository, InMemoryTierRepository, InMemoryUserRepository}
import tech.cryptonomic.nautilus.cloud.domain.UserService
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.resources.ResourceRepository
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.CreateUser
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.JsonMatchers

class UserRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with Fixtures
    with MockFactory {

  val userRepository = new InMemoryUserRepository[IO]()
  val apiKeyRepository = new InMemoryApiKeyRepository[IO]()
  val resourceRepository = stub[ResourceRepository[IO]]
  val tiersRepository = new InMemoryTierRepository[IO]()

  val sut = new UserRoutes(new UserService[IO](userRepository, apiKeyRepository, resourceRepository, tiersRepository))

  "The User route" should {

      "get user" in {
        // given
        userRepository.createUser(
          CreateUser(
            "email@example.com",
            Role.User,
            ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant,
            Github,
            None
          )
        )

        // when
        val result = Get("/users/1") ~> sut.getUserRoute(adminSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""{
                                                    |  "userId": 1,
                                                    |  "userRole": "user",
                                                    |  "userEmail": "email@example.com",
                                                    |  "registrationDate": "2019-05-27T17:03:48.081Z",
                                                    |  "accountSource": "github"
                                                    |}
                                                  """.stripMargin)
        }
      }

      "get 403 when trying to get user without admin role" in {
        Get("/users/1") ~> sut.getUserRoute(userSession) ~> check {
          status shouldEqual StatusCodes.Forbidden
        }
      }

      "get current user" in {
        // given
        userRepository.createUser(
          CreateUser(
            "email@example.com",
            Role.User,
            ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant,
            Github,
            None
          )
        )

        Get("/users/me") ~> sut.getCurrentUserRoute(userSession.copy(email = "email@example.com")) ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""{
                                                    |  "userId": 1,
                                                    |  "userRole": "user",
                                                    |  "userEmail": "email@example.com",
                                                    |  "registrationDate": "2019-05-27T17:03:48.081Z",
                                                    |  "accountSource": "github"
                                                    |}
                                                  """.stripMargin)
        }
      }

      "successfully update user" in {
        // given
        userRepository.createUser(exampleCreateUser.copy(userRole = Role.Administrator, accountDescription = None))

        // when
        val putRequest = HttpRequest(
            HttpMethods.PUT,
            uri = "/users/1",
            entity =
              HttpEntity(MediaTypes.`application/json`, """{"userRole": "user", "accountDescription": "description"}""")
          ) ~> sut.updateUserRoute(adminSession)

        // then
        putRequest ~> check {
          status shouldEqual StatusCodes.Created
        }

        Get("/users/1") ~> sut.getUserRoute(adminSession) ~> check {
          responseAs[String] should matchJson("""{"userRole": "user", "accountDescription": "description"}""")
        }
      }

      "get 403 when trying to update user without admin role" in {
        // when
        val request = HttpRequest(
            HttpMethods.PUT,
            uri = "/users/1",
            entity =
              HttpEntity(MediaTypes.`application/json`, """{"userRole": "user", "accountDescription": "description"}""")
          ) ~> sut.updateUserRoute(userSession)

        // then
        request ~> check {
          status shouldEqual StatusCodes.Forbidden
        }
      }

      "get user API keys" in {
        // given
        apiKeyRepository.add(
          ApiKey(keyId = 1, key = "apiKey", resourceId = 1, userId = 1, tierId = 1, dateIssued = None, dateSuspended = None)
        )

        // when
        val result = Get("/users/1/apiKeys") ~> sut.getUserKeysRoute

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""
                                                    |  [{
                                                    |    "resourceId": 1,
                                                    |    "tierId": 1,
                                                    |    "keyId": 1,
                                                    |    "key": "apiKey",
                                                    |    "userId": 1
                                                    |  }]
                                                  """.stripMargin)
        }
      }
    }
}
