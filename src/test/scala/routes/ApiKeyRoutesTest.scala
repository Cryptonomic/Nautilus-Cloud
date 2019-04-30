package routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.model.ApiKey
import tech.cryptonomic.cloud.nautilus.routes.ApiKeyRoutes
import tech.cryptonomic.cloud.nautilus.services.ApiKeyService


class ApiKeyRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonMatchers {

  "The API Keys route" should {

    val exampleApiKey = ApiKey(0, "", 1, 2, 3, None, None)

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
