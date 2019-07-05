package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.ApiKeyService
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyRepository
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures


class ApiKeyRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonMatchers with Fixtures with MockFactory {

  "The API Keys route" should {

    val apiKeyRepo = stub[ApiKeyRepository[IO]]


    val sut = new ApiKeyRoutes(new ApiKeyService[IO](apiKeyRepo))

      "return list containing one API key" in {
        (apiKeyRepo.getAllApiKeys _).when().returns(IO.pure(List(exampleApiKey)))
        Get("/apiKeys") ~> sut.getAllApiKeysRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson(exampleApiKeyAsJson)
        }
      }

      "return correctly validated API key" in {
        (apiKeyRepo.validateApiKey _).when("someApiKey").returns(IO.pure(true))
        Get("/apiKeys/someApiKey") ~> sut.validateApiKeyRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] shouldBe "true"
        }
      }

    }

}
