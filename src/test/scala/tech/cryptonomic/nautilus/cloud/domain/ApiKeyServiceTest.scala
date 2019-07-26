package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest.{EitherValues, Matchers, OneInstancePerTest, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.conseil.ConseilConfig
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryApiKeyRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures with EitherValues with OneInstancePerTest {

  val apiKeyRepo = new InMemoryApiKeyRepository()

  val conseilConf = ConseilConfig("key")
  val sut = new ApiKeyService[Id](apiKeyRepo, conseilConf)

  "ApiKeyService" should {
      "getAllApiKeys" in {
        apiKeyRepo.putApiKeyForUser(exampleCreateApiKey)

        val allApiKeys = sut.getAllApiKeys(adminSession).right.value

        allApiKeys shouldBe List(exampleApiKey)
      }
      "getAllApiKeysConseil" in {
        apiKeyRepo.putApiKeyForUser(exampleCreateApiKey)
        val allApiKeys = sut.getAllApiKeysForEnv("key", "").right.value

        allApiKeys shouldBe List(exampleApiKey.key)
      }

      "getAllApiKeysConseil should not authorize" in {
        apiKeyRepo.putApiKeyForUser(exampleCreateApiKey)
        val allApiKeys = sut.getAllApiKeysForEnv("wrong_key", "").left.get

        allApiKeys shouldBe a[AccessDenied]
      }
      "validateApiKey" in {
        apiKeyRepo.putApiKeyForUser(exampleCreateApiKey)

        val validationResult = sut.validateApiKey(exampleCreateApiKey.key)

        validationResult shouldBe true
      }
    }
}
