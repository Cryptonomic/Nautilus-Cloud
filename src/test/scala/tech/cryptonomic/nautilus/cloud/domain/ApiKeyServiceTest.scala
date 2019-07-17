package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest.EitherValues
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, CreateApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ApiKeyServiceTest extends WordSpec with Matchers with Fixtures with EitherValues {

  val apiKeyRepo = new ApiKeyRepository[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = true

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)

    /** Inserts API key */
    override def putApiKeyForUser(apiKey: CreateApiKey): Id[Unit] = ???

    /** Inserts API key usage */
    override def putApiKeyUsage(usageLeft: UsageLeft): Id[Unit] = ???

    /** Query returning API keys usage for given user */
    override def getKeysUsageForUser(userId: Int): Id[List[UsageLeft]] = ???

    /** Query returning API key usage */
    override def getKeyUsage(key: String): Id[Option[UsageLeft]] = ???

    /** Updates API key usage */
    override def updateKeyUsage(usage: UsageLeft): Id[Unit] = ???
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
