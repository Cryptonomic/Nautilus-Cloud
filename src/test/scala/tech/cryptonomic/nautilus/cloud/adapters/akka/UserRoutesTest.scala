package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.doobie.DoobieUniqueViolationException
import tech.cryptonomic.nautilus.cloud.domain.UserService
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.UserRepository
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class UserRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with Fixtures
    with MockFactory {

  val userRepository = stub[UserRepository[IO]]
  val apiKeyRepo = stub[ApiKeyRepository[IO]]

  val sut = new UserRoutes(new UserService[IO](userRepository, apiKeyRepo))

  "The User route" should {

      "successfully create user" in {
        (userRepository.createUser _).when(*).returns(IO.pure(Right(1)))

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

      "receive 409 Conflict response code when given email is already used" in {
        (userRepository.createUser _).when(*).returns(IO.pure(Left(DoobieUniqueViolationException("error"))))

        val postRequest = HttpRequest(
          HttpMethods.POST,
          uri = "/users",
          entity = HttpEntity(MediaTypes.`application/json`, exampleUserRegJson)
        )
        postRequest ~> sut.createUserRoute ~> check {
          status shouldEqual StatusCodes.Conflict
        }
      }

      "successfully update user" in {
        (userRepository.updateUser _).when(*, *).returns(IO.pure())

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
        (userRepository.getUser _).when(*).returns(IO.pure(Some(exampleUser)))

        Get("/users/1") ~> sut.getUserRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson(exampleUserJson)
        }
      }

      "get user API keys" in {
        (apiKeyRepo.getUserApiKeys _).when(*).returns(IO.pure(List(exampleApiKey)))

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
}
