package tech.cryptonomic.nautilus.cloud.adapters.akka

import java.time.ZonedDateTime

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContextWithInMemoryImplementations, JsonMatchers}

class UserRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with Fixtures
    with MockFactory
    with OneInstancePerTest {

  val context = new DefaultNautilusContextWithInMemoryImplementations
  val userRepository = context.userRepository
  val apiKeyRepository = context.apiKeyRepository
  val sut = context.userRoutes

  "The User route" should {

      "get user" in {
        // given
        userRepository.createUser(
          exampleCreateUser.copy(
            userEmail = "email@example.com",
            userRole = Role.User,
            registrationDate = ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant,
            accountSource = Github
          )
        )

        // when
        val result = Get("/users/1") ~> sut.getUserRoute(adminSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson(
            """{
                                                    |  "userId": 1,
                                                    |  "userRole": "user",
                                                    |  "userEmail": "email@example.com",
                                                    |  "registrationDate": "2019-05-27T17:03:48.081Z",
                                                    |  "accountSource": "github"
                                                    |}
                                                  """.stripMargin // @todo additional fields
          )
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
          exampleCreateUser.copy(
            userEmail = "email@example.com",
            userRole = Role.User,
            registrationDate = ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant,
            accountSource = Github
          )
        )

        Get("/users/me") ~> sut.getCurrentUserRoute(
          userSession.copy(email = "email@example.com")
        ) ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson(
            """{
                                                    |  "userId": 1,
                                                    |  "userRole": "user",
                                                    |  "userEmail": "email@example.com",
                                                    |  "registrationDate": "2019-05-27T17:03:48.081Z",
                                                    |  "accountSource": "github"
                                                    |}
                                                  """.stripMargin
          )
        }
      }

      "successfully update user" in {
        // given
        userRepository.createUser(
          exampleCreateUser
            .copy(userRole = Role.Administrator, accountDescription = None)
        )

        // when
        val putRequest = HttpRequest(
            HttpMethods.PUT,
            uri = "/users/1",
            entity = HttpEntity(
              MediaTypes.`application/json`,
              """{"userRole": "user", "accountDescription": "description"}"""
            )
          ) ~> sut.updateUserRoute(adminSession)

        // then
        putRequest ~> check {
          status shouldEqual StatusCodes.Created
        }

        Get("/users/1") ~> sut.getUserRoute(adminSession) ~> check {
          responseAs[String] should matchJson(
            """{"userRole": "user", "accountDescription": "description"}"""
          )
        }
      }

      "delete current user" in {
        // given
        userRepository.createUser(exampleCreateUser)

        Get("/users/me") ~> sut.getCurrentUserRoute(userSession) ~> check {
          status shouldEqual StatusCodes.OK
        }

        // when
        val putResponse = Delete("/users/me") ~> sut.deleteCurrentUserRoute(userSession)

        // then
        putResponse ~> check {
          status shouldEqual StatusCodes.OK
        }

        Get("/users/me") ~> sut.getCurrentUserRoute(userSession) ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }

      "delete a user" in {
        // given
        userRepository.createUser(exampleCreateUser)

        Get("/users/1") ~> sut.getUserRoute(adminSession) ~> check {
          status shouldEqual StatusCodes.OK
        }

        // when
        val deleteResponse = Delete("/users/1") ~> sut.deleteUserRoute(adminSession)

        // then
        deleteResponse ~> check {
          status shouldEqual StatusCodes.OK
        }

        Get("/users/1") ~> sut.getUserRoute(adminSession) ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }

      "get all users" in {
        // given
        userRepository.createUser(exampleCreateUser)

        // when
        val usersResponse = Get("/users") ~> sut.getUsersRoute(adminSession)

        // then
        usersResponse ~> check {
          status shouldEqual StatusCodes.OK
          responseAs[String] should matchJson(
            """{
              |  "pagesTotal": 1,
              |  "resultCount": 1,
              |  "result": [
              |    {
              |      "userId": 1,
              |      "userRole": "user",
              |      "userEmail": "email@example.com",
              |      "registrationDate": "2019-05-27T17:03:48.081Z",
              |      "accountSource": "github"
              |    }
              |  ]
              |}""".stripMargin
          )
        }
      }

      "get users filtered by email address" in {
        // given
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test1@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test2@domain.com"))

        // when
        val usersResponse = Get("/users?email=test1") ~> sut.getUsersRoute(adminSession)

        // then
        usersResponse ~> check {
          status shouldEqual StatusCodes.OK
          responseAs[String] should matchJson(
            """{
              |  "pagesTotal": 1,
              |  "resultCount": 1,
              |  "result": [
              |    {
              |      "userEmail": "test1@domain.com"
              |    }
              |  ]
              |}""".stripMargin
          )
        }
      }

      "get users filtered by id" in {
        // given
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test1@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test2@domain.com"))

        // when
        val usersResponse = Get("/users?userId=2") ~> sut.getUsersRoute(adminSession)

        // then
        usersResponse ~> check {
          status shouldEqual StatusCodes.OK
          responseAs[String] should matchJson(
            """{
              |  "pagesTotal": 1,
              |  "resultCount": 1,
              |  "result": [
              |    {
              |      "userEmail": "test2@domain.com"
              |    }
              |  ]
              |}""".stripMargin
          )
        }
      }

      "get users filtered by api key" in {
        // given
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test1@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test2@domain.com"))

        apiKeyRepository.putApiKey(exampleCreateApiKey.copy(key = "some-api-key-1", userId = 1))
        apiKeyRepository.putApiKey(exampleCreateApiKey.copy(key = "some-api-key-2", userId = 2))

        // when
        val usersResponse = Get("/users?apiKey=key-1") ~> sut.getUsersRoute(adminSession)

        // then
        usersResponse ~> check {
          status shouldEqual StatusCodes.OK
          responseAs[String] should matchJson(
            """{
              |  "pagesTotal": 1,
              |  "resultCount": 1,
              |  "result": [
              |    {
              |      "userEmail": "test1@domain.com"
              |    }
              |  ]
              |}""".stripMargin
          )
        }
      }

      "paginate users" in {
        // given
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test1@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "test2@domain.com"))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "different-login@domain.com"))

        // when
        val usersResponse = Get("/users?email=test&page=1&limit=1") ~> sut.getUsersRoute(adminSession)

        // then
        usersResponse ~> check {
          status shouldEqual StatusCodes.OK
          responseAs[String] should matchJson(
            """{
              |  "pagesTotal": 2,
              |  "resultCount": 2,
              |  "result": [
              |    {
              |      "userEmail": "test1@domain.com"
              |    }
              |  ]
              |}""".stripMargin
          )
        }
      }

      "get 403 when trying to update user without admin role" in {
        // when
        val request = HttpRequest(
            HttpMethods.PUT,
            uri = "/users/1",
            entity = HttpEntity(
              MediaTypes.`application/json`,
              """{"userRole": "user", "accountDescription": "description"}"""
            )
          ) ~> sut.updateUserRoute(userSession)

        // then
        request ~> check {
          status shouldEqual StatusCodes.Forbidden
        }
      }
    }
}
