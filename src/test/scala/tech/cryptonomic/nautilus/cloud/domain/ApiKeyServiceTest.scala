package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Id
import org.scalatest.EitherValues
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.{
  InMemoryApiKeyRepository,
  InMemoryResourceRepository,
  InMemoryTierRepository
}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyGenerator, CreateApiKey, Environment}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.FixedClock

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures with EitherValues {

  val apiKeyRepo = new InMemoryApiKeyRepository()
  val resourceRepo = new InMemoryResourceRepository()
  val tiersRepo = new InMemoryTierRepository()
  val apiKeyGenerator = new ApiKeyGenerator()

  val sut =
    new ApiKeyService[Id](apiKeyRepo, resourceRepo, tiersRepo, new FixedClock[Id](Instant.now()), apiKeyGenerator)

  "ApiKeyService" should {
      "getAllApiKeys" in {
        apiKeyRepo.putApiKey(
          CreateApiKey(
            key = "c3686d8c-59ea-430e-97f3-90977952ab8b",
            environment = Environment.Development,
            userId = 1,
            tierId = 2,
            dateIssued = time,
            dateSuspended = None
          )
        )

        val allApiKeys = sut.getAllApiKeys(adminSession).right.value

        allApiKeys shouldBe List(
          ApiKey(
            keyId = 1,
            key = "c3686d8c-59ea-430e-97f3-90977952ab8b",
            environment = Environment.Development,
            userId = 1,
            tierId = 2,
            dateIssued = Some(time),
            dateSuspended = None
          )
        )
      }
      "validateApiKey" in {
        apiKeyRepo.putApiKey(
          CreateApiKey(
            key = "c3686d8c-59ea-430e-97f3-90977952ab8b",
            environment = Environment.Development,
            userId = 1,
            tierId = 2,
            dateIssued = time,
            dateSuspended = None
          )
        )

        val validationResult = sut.validateApiKey("c3686d8c-59ea-430e-97f3-90977952ab8b")

        validationResult shouldBe true
      }
    }
}
