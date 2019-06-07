package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.UserService
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.{User, UserRepository, UserWithoutId}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class UserRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonMatchers with Fixtures {

  "The User route" should {

    val userRepository = new UserRepository[IO] {
      override def createUser(userReg: UserWithoutId): IO[Int] = IO.pure(1)

      override def updateUser(user: User): IO[Unit] = IO.pure(())

      override def getUser(userId: Int): IO[Option[User]] = IO.pure(Some(exampleUser))
    }

    val apiKeyRepo = new ApiKeyRepository[IO] {
      override def getUserApiKeys(userId: Int): IO[List[ApiKey]] = IO.pure(List(exampleApiKey))

      override def getAllApiKeys: IO[List[ApiKey]] = ???

      override def validateApiKey(apiKey: String): IO[Boolean] = ???

      override def getKeysUsageForUser(userId: Int): IO[List[UsageLeft]] = IO.pure(List(examleUsageLeft))

      override def getKeyUsage(key: String): IO[Option[UsageLeft]] = ???

      override def updateKeyUsage(usage: UsageLeft): IO[Unit] = ???
    }

    val sut = new UserRoutes(new UserService[IO](userRepository, apiKeyRepo))

    "successfully create user" in {
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/users",
        entity = HttpEntity(MediaTypes.`application/json`, exampleUserRegJson)
      )
      postRequest ~> sut.createUserRoute ~> check {
        status shouldEqual StatusCodes.Created
        responseAs[String] shouldBe "1"
      }
    }

    "successfully update user" in {
      val putRequest = HttpRequest(
        HttpMethods.PUT,
        uri = "/users/1",
        entity = HttpEntity(MediaTypes.`application/json`, exampleUserJson)
      )
      putRequest ~> sut.updateUserRoute ~> check {
        status shouldEqual StatusCodes.Created
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
    "get user API keys usage" in {
      Get("/users/1/usage") ~> sut.getApiKeyUsageRoute ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String] should matchJson(exampleUsageJson)
      }
    }
  }
}
