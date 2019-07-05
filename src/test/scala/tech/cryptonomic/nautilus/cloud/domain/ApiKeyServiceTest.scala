package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyRepository
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures with MockFactory {

  val apiKeyRepo = stub[ApiKeyRepository[Id]]

  val sut = new ApiKeyService[Id](apiKeyRepo)

  "ApiKeyService" should {
    "getAllApiKeys" in {
      (apiKeyRepo.getAllApiKeys _).when().returns(List(exampleApiKey))
      sut.getAllApiKeys shouldBe List(exampleApiKey)
    }
    "validateApiKey" in {
      (apiKeyRepo.validateApiKey _).when("xyz").returns(true)
      sut.validateApiKey("xyz") shouldBe true
    }
  }
}
