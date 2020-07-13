package tech.cryptonomic.nautilus.cloud.adapters.akka

import java.time.Instant

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.Environment.Production
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, Environment, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.tier.Usage
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContextWithInMemoryImplementations, FixedApiKeyGenerator, FixedClock, JsonMatchers}

class ApiKeyRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with Fixtures
    with OneInstancePerTest {

  "The API Keys route" should {

      val context = new DefaultNautilusContextWithInMemoryImplementations {
        implicit override val clock: FixedClock[IO] = new FixedClock(now)
        override lazy val apiKeyGenerator = new FixedApiKeyGenerator()
      }
      val userRepository = context.userRepository
      val apiKeyRepository = context.apiKeyRepository
      val meteringStatsRepository = context.meteringStatsRepository
      val sut = context.apiKeysRoutes
      val meteringApiRepository = context.meteringApiRepository

      "return list containing one api key" in {
        // when
        context.apiKeyRepository.add(
          ApiKey(keyId = 0, key = "", Environment.Development, userId = 2, dateIssued = None, dateSuspended = None)
        )

        // expect
        Get("/apiKeys") ~> sut.getApiKeysRoute(adminSession) ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""
                                                    |  [{
                                                    |    "environment": "dev",
                                                    |    "keyId": 0,
                                                    |    "key": "",
                                                    |    "userId": 2
                                                    |  }]
                                                  """.stripMargin)
        }
      }

      "return correctly validated api key" in {
        // when
        context.apiKeyRepository.add(exampleApiKey.copy(key = "someApiKey"))

        // expect
        Get("/apiKeys/someApiKey/valid") ~> sut.validateApiKeyRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] shouldBe "true"
        }
      }

      "refresh api key" in {
        // given
        context.apiKeyRepository.add(exampleApiKey.copy(key = "someApiKey", environment = Production))

        // when
        val result = Post("/users/me/apiKeys/prod/refresh") ~> sut.refreshKeysRoute(userSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson(s"""{
                                                |  "key": "exampleApiKey0",
                                                |  "environment": "prod",
                                                |  "userId": 1,
                                                |  "dateIssued": "${context.now}"
                                                |}""".stripMargin)
        }

        Get("/users/me/apiKeys") ~> sut.getCurrentUserKeysRoute(userSession) ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson(s"""[
                                                |  {
                                                |    "key": "exampleApiKey0",
                                                |    "environment": "prod",
                                                |    "userId": 1,
                                                |    "dateIssued": "${context.now}"
                                                |  }
                                                |]""".stripMargin)
        }
      }

      "get user API keys" in {
        // given
        apiKeyRepository.add(
          ApiKey(
            keyId = 1,
            key = "apiKey",
            environment = Environment.Development,
            userId = 1,
            dateIssued = None,
            dateSuspended = None
          )
        )

        // when
        val result = Get("/users/1/apiKeys") ~> sut.getUserKeysRoute(adminSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""
                                              |  [{
                                              |    "environment": "dev",
                                              |    "keyId": 1,
                                              |    "key": "apiKey",
                                              |    "userId": 1
                                              |  }]
                                                  """.stripMargin)
        }
      }

      "get current user API keys" in {
        // given
        apiKeyRepository.add(
          ApiKey(
            keyId = 1,
            key = "apiKey",
            environment = Environment.Production,
            userId = 1,
            dateIssued = None,
            dateSuspended = None
          )
        )
        userRepository.createUser(exampleCreateUser)

        // when
        val result = Get("/users/me/apiKeys") ~> sut.getCurrentUserKeysRoute(userSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""
                                              |  [{
                                              |    "environment": "prod",
                                              |    "keyId": 1,
                                              |    "key": "apiKey",
                                              |    "userId": 1
                                              |  }]
                                            """.stripMargin)
        }
      }

      "return list of api keys with a single key from conseil route" in {
        // when
        context.apiKeyRepository.add(exampleApiKey.copy(key = "someApiKey"))

        // expect
        Get("/apiKeys/dev") ~> addHeader("X-Api-Key", "exampleApiKey") ~> sut.getAllApiKeysForEnvRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""["someApiKey"]""")
        }
      }

      "return 403 when uses wrong conseil key" in {
        // when
        context.apiKeyRepository.add(exampleApiKey.copy(key = "someApiKey"))

        // expect
        Get("/apiKeys/dev") ~> addHeader("X-Api-Key", "wrong_key") ~> sut.getAllApiKeysForEnvRoute ~> check {
          status shouldEqual StatusCodes.Forbidden
        }
      }

      "get current user API key usage" in {
        // given
        apiKeyRepository.add(exampleApiKey.copy(key = "apiKey", userId = 1))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "email@example.com"))
        apiKeyRepository.putApiKeyUsage(
          UsageLeft(
            key = "apiKey",
            Usage(daily = 10, monthly = 100)
          )
        )

        // when
        val result = Get("/users/me/usage") ~> sut.getCurrentKeyUsageRoute(
                userSession.copy(email = "email@example.com")
              )

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""
                                              |  [{
                                              |    "key": "apiKey",
                                              |    "usage": {
                                              |      "daily": 10,
                                              |      "monthly": 100
                                              |    }
                                              |  }]
                                            """.stripMargin)
        }
      }

      "get API key stats" in {
        // given
        apiKeyRepository.add(exampleApiKey.copy(key = "apikey", userId = 1, dateIssued = Some(Instant.now())))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "email@example.com"))
        meteringApiRepository.addApiKeyStats5m(exampleApiKeyStats5m)
        meteringApiRepository.addApiKeyStats24h(exampleApiKeyStats24h)
        meteringApiRepository.addRouteStats5m(exampleRouteStats5m)
        meteringApiRepository.addRouteStats24h(exampleRouteStats24h)
        meteringApiRepository.addIpStats5m(exampleIpStats5m)
        meteringApiRepository.addIpStats24h(exampleIpStats24h)

        // when
        val result = Get("/users/me/stats") ~> sut.getCurrentUserApiKeyQueryStatsRoute(
                userSession.copy(email = "email@example.com")
              )

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""{
                                                |  "apiKeyStats5m":[
                                                |    {
                                                |      "time":"2019-05-26T23:05:00Z",
                                                |      "count":1,
                                                |      "apiKey":"apikey"
                                                |    }
                                                |  ],
                                                |  "apiKeyStats24h":[
                                                |    {
                                                |      "time":"2019-05-26T23:00:00Z",
                                                |      "count":1,
                                                |      "apiKey":"apikey"
                                                |    }
                                                |  ],
                                                |  "routeStats5m":[
                                                |    {
                                                |      "time":"2019-05-26T23:05:00Z",
                                                |      "count":1,
                                                |      "uri":"url",
                                                |      "apiKey":"apikey"
                                                |    }
                                                |  ],
                                                |  "routeStats24h":[
                                                |    {
                                                |      "time":"2019-05-26T23:00:00Z",
                                                |      "count":1,
                                                |      "uri":"url",
                                                |      "apiKey":"apikey"
                                                |    }
                                                |  ],
                                                |  "ipStats5m":[
                                                |    {
                                                |      "time":"2019-05-26T23:05:00Z",
                                                |      "count":1,
                                                |      "ip":"ip",
                                                |      "apiKey":"apikey"
                                                |    }
                                                |  ],
                                                |  "ipStats24h":[
                                                |    {
                                                |      "time":"2019-05-26T23:00:00Z",
                                                |      "count":1,
                                                |      "ip":"ip",
                                                |      "apiKey":"apikey"
                                                |    }
                                                |  ]
                                                |}""".stripMargin)
        }

      }

      "get API key aggregated statistics" in {
        // given
        meteringStatsRepository.insertStats(exampleAggregatedMeteringStats)
        userRepository.createUser(exampleCreateUser.copy(userEmail = "email@example.com"))

        // when
        val result = Get("/users/1/stats/aggregated") ~> sut.getApiKeyAggregatedStatsRoute(
                adminSession.copy(email = "email@example.com")
              )

        // then
        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""[{
                                                |	"userId": 1,
                                                |	"environment": "environment",
                                                |	"hits": 42,
                                                |	"periodStart": "2019-05-26T23:05:00Z",
                                                |	"periodEnd": "2019-05-26T23:10:00Z"
                                                |}]""".stripMargin)
        }
      }
    }
}
