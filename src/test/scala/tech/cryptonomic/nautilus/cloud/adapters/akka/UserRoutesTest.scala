package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.doobie.DoobieUniqueViolationException
import tech.cryptonomic.nautilus.cloud.domain.UserService
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyRepository
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
    }
}
