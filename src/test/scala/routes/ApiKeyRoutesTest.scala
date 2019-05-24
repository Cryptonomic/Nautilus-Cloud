package routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import fixtures.Fixtures
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.routes.ApiKeyRoutes
import tech.cryptonomic.nautilus.cloud.services.ApiKeyService
import tech.cryptonomic.nautilus.cloud.model.ApiKey


class ApiKeyRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonMatchers with Fixtures {

  "The API Keys route" should {

    val apiKeyService = new ApiKeyService[IO] {
      override def getAllApiKeys: IO[List[ApiKey]] = IO.pure(List(exampleApiKey))

      override def validateApiKey(apiKey: String): IO[Boolean] = IO.pure(true)
    }
    val sut = new ApiKeyRoutes(apiKeyService)

    "return list containing one api key" in {
      Get("/apiKeys") ~> sut.getAllApiKeysRoute ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String] should matchJson(exampleApiKeyAsJson)
      }
    }

    "return correctly validated api key" in {
      Get("/apiKeys/someApiKey") ~> sut.validateApiKeyRoute ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String] shouldBe "true"
      }
    }
  }

}
