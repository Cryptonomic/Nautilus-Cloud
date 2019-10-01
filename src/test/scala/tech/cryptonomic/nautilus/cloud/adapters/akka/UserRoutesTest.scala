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
          CreateUser(
            "email@example.com",
            Role.User,
            ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant,
            Github,
            1,
            None
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
                                                  """.stripMargin
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
          CreateUser(
            "email@example.com",
            Role.User,
            ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant,
            Github,
            1,
            None
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
        val usersResponse = Get("/users") ~> sut.getAllUsersRoute(adminSession)

        // then
        usersResponse ~> check {
          status shouldEqual StatusCodes.OK
          responseAs[String] should matchJson(
            """[
              |  {
              |    "userId": 1,
              |    "userRole": "user",
              |    "userEmail": "email@example.com",
              |    "registrationDate": "2019-05-27T17:03:48.081Z",
              |    "accountSource": "github"
              |  }
              |]""".stripMargin
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
