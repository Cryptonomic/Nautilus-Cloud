package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest.EitherValues
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures with EitherValues {

  val apiKeyRepo = new ApiKeyRepository[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = true

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)
  }

  val sut = new ApiKeyService[Id](apiKeyRepo)

  "ApiKeyService" should {
    "getAllApiKeys" in {
      sut.getAllApiKeys(adminSession).right.value shouldBe List(exampleApiKey)
    }
    "validateApiKey" in {
      sut.validateApiKey("") shouldBe true
    }
  }
}
