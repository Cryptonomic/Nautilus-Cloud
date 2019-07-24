package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest.EitherValues
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryApiKeyRepository
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures with EitherValues {

  val apiKeyRepo = new InMemoryApiKeyRepository()

  val sut = new ApiKeyService[Id](apiKeyRepo)

  "ApiKeyService" should {
    "getAllApiKeys" in {
      apiKeyRepo.putApiKeyForUser(exampleCreateApiKey)

      val allApiKeys = sut.getAllApiKeys(adminSession).right.value

      allApiKeys shouldBe List(exampleApiKey)
    }
    "validateApiKey" in {
      apiKeyRepo.putApiKeyForUser(exampleCreateApiKey)

      val validationResult = sut.validateApiKey(exampleCreateApiKey.key)

      validationResult shouldBe true
    }
  }
}
