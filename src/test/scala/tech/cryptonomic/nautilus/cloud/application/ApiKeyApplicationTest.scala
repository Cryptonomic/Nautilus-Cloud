package tech.cryptonomic.nautilus.cloud.application

import org.scalatest.{EitherValues, Matchers, OneInstancePerTest, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, Environment}
import tech.cryptonomic.nautilus.cloud.domain.tier.Usage
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.IdContext

class ApiKeyApplicationTest extends WordSpec with Matchers with Fixtures with EitherValues with OneInstancePerTest {

  val context: IdContext = new IdContext {}
  val sut = context.apiKeyApplication
  val apiKeyService = context.apiKeyService
  val apiKeyRepository = context.apiKeyRepository
  val now = context.now

  "ApiKeyApplication" should {
      "initialize ApiKeys" in {
        // given
        apiKeyService.initializeApiKeys(userId = 1, Usage(1, 2))

        // when
        val result = sut.getAllApiKeys(adminSession).right.value

        // then
        result shouldBe List(
          ApiKey(1, "exampleApiKey0", Environment.Production, 1, Some(now), None),
          ApiKey(2, "exampleApiKey1", Environment.Development, 1, Some(now), None)
        )
      }

      "validateApiKey" in {
        // given
        apiKeyService.initializeApiKeys(userId = 1, Usage.default) // creates two api keys: exampleApiKey0 and exampleApiKey1

        // expect
        sut.validateApiKey("exampleApiKey0") shouldBe true
        sut.validateApiKey("exampleApiKey1") shouldBe true
        sut.validateApiKey("exampleApiKey2") shouldBe false
      }

      "refresh ApiKeys" in {
        // given
        apiKeyService.initializeApiKeys(userId = 1, Usage.default)

        // when
        sut.refreshApiKey(Environment.Development)(userSession)

        // then
        sut.getAllApiKeys(adminSession).right.value shouldBe List(
          ApiKey(1, "exampleApiKey0", Environment.Production, 1, Some(now), None),
          ApiKey(2, "exampleApiKey1", Environment.Development, 1, Some(now), Some(now)),
          ApiKey(3, "exampleApiKey2", Environment.Development, 1, Some(now), None)
        )
      }

      "get active ApiKeys" in {
        // given
        apiKeyService.initializeApiKeys(userId = 1, Usage.default)
        sut.refreshApiKey(Environment.Development)(userSession)

        // when
        val result = sut.getCurrentUserApiKeys(adminSession)

        // then
        result shouldBe List(
          ApiKey(1, "exampleApiKey0", Environment.Production, 1, Some(now), None),
          ApiKey(3, "exampleApiKey2", Environment.Development, 1, Some(now), None)
        )
      }

      "get user api keys" in {
        // given
        apiKeyRepository.add(exampleApiKey.copy(keyId = 1, userId = 1))
        apiKeyRepository.add(exampleApiKey.copy(keyId = 2, userId = 1))
        apiKeyRepository.add(exampleApiKey.copy(keyId = 3, userId = 2))

        // expect
        sut.getApiKeys(1)(adminSession).right.value.map(_.keyId) shouldBe List(1, 2)
      }
    }
}
