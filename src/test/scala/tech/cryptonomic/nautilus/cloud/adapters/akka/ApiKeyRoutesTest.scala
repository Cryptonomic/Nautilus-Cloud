package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.ApiKeyService
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ApiKeyRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonMatchers with Fixtures {

  "The API Keys route" should {

    val apiKeyRepository = new ApiKeyRepository[IO]() {
      override def getAllApiKeys: IO[List[ApiKey]] = IO.pure(List(exampleApiKey))

      override def validateApiKey(apiKey: String): IO[Boolean] = IO.pure(true)

      override def getUserApiKeys(userId: Int): IO[List[ApiKey]] = ???

      override def getKeysUsageForUser(userId: Int): IO[List[UsageLeft]] = ???

      override def getKeyUsage(key: String): IO[Option[UsageLeft]] = ???

      override def updateKeyUsage(usage: UsageLeft): IO[Unit] = ???
    }

    val sut = new ApiKeyRoutes(new ApiKeyService[IO](apiKeyRepository))

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
