package routes

import java.sql.Timestamp

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserRegistration}
import tech.cryptonomic.cloud.nautilus.routes.UserRoutes
import tech.cryptonomic.cloud.nautilus.services.UserService

class UserRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonMatchers {

  "The API Keys route" should {

    val exampleApiKey = ApiKey(0, "", 1, 2, 3, None, None)

    val exampleUser = User(1, "someUserName", "email@example.com", "user", new Timestamp(1), None, None)

    val exampleApiKeyAsJson =
      """
        |  [{
        |    "resourceId": 1,
        |    "tierId": 3,
        |    "keyId": 0,
        |    "key": "",
        |    "userId": 2
        |  }]
      """.stripMargin

    val exampleUserRegJson =
      """
        |{
        |  "userRole": "user",
        |  "userEmail": "email@example.com",
        |  "registrationDate": 1,
        |  "userName": "someUserName"
        |}
      """.stripMargin

    val exampleUserJson =
      """
        |{
        |  "userId": 1,
        |  "userRole": "user",
        |  "userEmail": "email@example.com",
        |  "registrationDate": 1,
        |  "userName": "someUserName"
        |}
      """.stripMargin



    val userService = new UserService[IO] {
      override def createUser(userReg: UserRegistration): IO[Unit] = IO.pure(())

      override def updateUser(user: User): IO[Unit] = IO.pure(())

      override def getUser(userId: Int): IO[Option[User]] = IO.pure(Some(exampleUser))

      override def getUserApiKeys(userId: Int): IO[List[ApiKey]] = IO.pure(List(exampleApiKey))
    }

    val sut = new UserRoutes(userService)

    "successfully create user" in {
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/users",
        entity = HttpEntity(MediaTypes.`application/json`, exampleUserRegJson)
      )
      postRequest ~> sut.createUserRoute ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "successfully update user" in {
      val putRequest = HttpRequest(
        HttpMethods.PUT,
        uri = "/users",
        entity = HttpEntity(MediaTypes.`application/json`, exampleUserJson)
      )
      putRequest ~> sut.updateUserRoute ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "get user" in {
      Get("/users/1") ~> sut.getUserRoute ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String] should matchJson(exampleUserJson)
      }
    }

    "get user API keys" in {
      Get("/users/1/apiKeys") ~> sut.getUserKeysRoute ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String] should matchJson(exampleApiKeyAsJson)
      }
    }
  }
}
