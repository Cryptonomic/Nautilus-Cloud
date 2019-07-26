package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.conseil.ConseilConfig
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryApiKeyRepository
import tech.cryptonomic.nautilus.cloud.domain.ApiKeyService
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ApiKeyRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with Fixtures
    with OneInstancePerTest {

  "The API Keys route" should {

      val apiKeyRepository = new InMemoryApiKeyRepository[IO]()
      val conseilConf = ConseilConfig("key")

      val sut = new ApiKeyRoutes(new ApiKeyService[IO](apiKeyRepository, conseilConf))

      "return list containing one api key" in {
        // when
        apiKeyRepository.add(
          ApiKey(keyId = 0, key = "", resourceId = 1, userId = 2, tierId = 3, dateIssued = None, dateSuspended = None)
        )

        // expect
        Get("/apiKeys") ~> sut.getAllApiKeysRoute(adminSession) ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""
                                                    |  [{
                                                    |    "resourceId": 1,
                                                    |    "tierId": 3,
                                                    |    "keyId": 0,
                                                    |    "key": "",
                                                    |    "userId": 2
                                                    |  }]
                                                  """.stripMargin)
        }
      }

      "return correctly validated api key" in {
        // when
        apiKeyRepository.add(exampleApiKey.copy(key = "someApiKey"))

        // expect
        Get("/apiKeys/someApiKey") ~> sut.validateApiKeyRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] shouldBe "true"
        }
      }

      "return list of api keys with a single key from conseil route" in {
        // when
        apiKeyRepository.add(exampleApiKey.copy(key = "someApiKey"))

        // expect
        Get("/conseil/apiKeys") ~> addHeader("X-Api-Key", "key") ~> sut.getAllApiKeysConseilRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""["someApiKey"]""")
        }
      }

      "return 403 when uses wrong conseil key" in {
        // when
        apiKeyRepository.add(exampleApiKey.copy(key = "someApiKey"))

        // expect
        Get("/conseil/apiKeys") ~> addHeader("X-Api-Key", "wrong_key") ~> sut.getAllApiKeysConseilRoute ~> check {
          status shouldEqual StatusCodes.Forbidden
        }
      }

    }

}
