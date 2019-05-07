package services

import cats.Id
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.model.ApiKey
import tech.cryptonomic.cloud.nautilus.repositories.ApiKeyRepo
import tech.cryptonomic.cloud.nautilus.services.ApiKeyServiceImpl

class ApiKeyServiceTest extends WordSpec with Matchers {

  val exampleApiKey = ApiKey(0, "", 1, 2, 3, None, None)

  val apiKeyRepo = new ApiKeyRepo[Id] {
    override def getAllApiKeys: Id[List[ApiKey]] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Id[Boolean] = true

    override def getUserApiKeys(userId: Int): Id[List[ApiKey]] = List(exampleApiKey)
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
