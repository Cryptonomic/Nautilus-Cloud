package services

import cats.Id
import fixtures.Fixtures
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.model.ApiKey
import tech.cryptonomic.cloud.nautilus.repositories.ApiKeyRepo
import tech.cryptonomic.cloud.nautilus.services.ApiKeyServiceImpl

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures {

  val apiKeyRepo = new ApiKeyRepo[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = true

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)
  }

  val sut = new ApiKeyServiceImpl[Id](apiKeyRepo)

  "ApiKeyService" should {
    "getAllApiKeys" in {
      sut.getAllApiKeys shouldBe List(exampleApiKey)
    }
    "validateApiKey" in {
      sut.validateApiKey("") shouldBe true
    }
  }
}
