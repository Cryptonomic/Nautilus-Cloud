package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, Environment}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.DefaultNautilusContextWithInMemoryImplementations

class ApiKeyRoutesTest extends WordSpec with Matchers with ScalatestRouteTest with JsonMatchers with Fixtures {

  "The API Keys route" should {

      val context = new DefaultNautilusContextWithInMemoryImplementations

      val sut = context.apiKeysRoutes

      "return list containing one api key" in {
        // when
        context.apiKeysRepository.add(
          ApiKey(keyId = 0, key = "", Environment.Development, userId = 2, tierId = 3, dateIssued = None, dateSuspended = None)
        )

        // expect
        Get("/apiKeys") ~> sut.getAllApiKeysRoute(adminSession) ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""
                                                    |  [{
                                                    |    "environment": "dev",
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
        context.apiKeysRepository.add(exampleApiKey.copy(key = "someApiKey"))

        // expect
        Get("/apiKeys/someApiKey") ~> sut.validateApiKeyRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] shouldBe "true"
        }
      }
    }

}
