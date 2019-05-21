package tech.cryptonomic.cloud.nautilus.domain

import cats.Id
import tech.cryptonomic.cloud.nautilus.fixtures.Fixtures
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.domain.apiKey.{ApiKey, ApiKeyRepository}

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures {

  val apiKeyRepo = new ApiKeyRepository[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = true

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)
  }

  val sut = new ApiKeyService[Id](apiKeyRepo)

  "ApiKeyService" should {
    "getAllApiKeys" in {
      sut.getAllApiKeys shouldBe List(exampleApiKey)
    }
    "validateApiKey" in {
      sut.validateApiKey("") shouldBe true
    }
  }
}
