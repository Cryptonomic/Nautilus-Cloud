package tech.cryptonomic.nautilus.cloud.adapters.akka

import java.time.{Instant, ZonedDateTime}

import akka.http.scaladsl.model.ContentTypes.NoContentType
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.{Clock, IO}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.tier.{TierConfiguration, TierName, Usage}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{
  DefaultNautilusContextWithInMemoryImplementations,
  FixedClock,
  JsonMatchers
}

class TierRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with BeforeAndAfterEach
    with Fixtures
    with MockFactory {

  val now = ZonedDateTime.parse("2019-05-27T12:03:48.081+01:00").toInstant

  val context = new DefaultNautilusContextWithInMemoryImplementations() {
    override val clock: Clock[IO] = new FixedClock[IO](now)
  }

  val tierRepository = context.tierRepository
  val sut = context.tierRoutes

  override def beforeEach() = tierRepository.clear()

  "The Tier route" should {

      "create tier" in {
        // when
        val response: RouteTestResult = HttpRequest(
            HttpMethods.PUT,
            uri = "/tiers/a_b",
            entity = HttpEntity(
              MediaTypes.`application/json`,
              """{
                                                      |  "description": "some description",
                                                      |  "usage": {
                                                      |    "daily": 10,
                                                      |    "monthly": 100
                                                      |  },
                                                      |  "maxResultSetSize": 20
                                                      |}""".stripMargin
            )
          ) ~> sut.createTierRoute(adminSession)

        // then
        response ~> check {
          status shouldEqual StatusCodes.Created
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""{
                                                |  "name": "a_b",
                                                |  "configurations": [
                                                |    {
                                                |      "description": "some description",
                                                |      "usage": {
                                                |        "daily": 10,
                                                |        "monthly": 100
                                                |      },
                                                |      "maxResultSetSize": 20
                                                |    }
                                                |  ]
                                                |}""".stripMargin)
        }
      }

      "update tier" in {
        // given
        tierRepository.create(TierName("a_b"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // when
        val response: RouteTestResult = HttpRequest(
            HttpMethods.POST,
            uri = "/tiers/a_b/configurations",
            entity = HttpEntity(
              MediaTypes.`application/json`,
              """{
              |  "description": "some other description",
              |  "usage": {
              |    "daily": 10,
              |    "monthly": 100
              |  },
              |  "maxResultSetSize": 20,
              |  "startDate": "2019-05-27T18:03:48.081+01:00"
              |}""".stripMargin
            )
          ) ~> sut.updateTierRoute(adminSession)

        // then
        response ~> check {
          status shouldEqual StatusCodes.Created
          contentType shouldBe NoContentType
        }

        Get("/tiers/a_b") ~> sut.getTierRoute(adminSession) ~> check {
          responseAs[String] should matchJson("""{
              |  "name": "a_b",
              |  "configurations": [
              |    {
              |      "description": "description",
              |      "usage": {
              |        "daily": 1,
              |        "monthly": 2
              |      },
              |      "maxResultSetSize": 3,
              |      "startDate": "2019-05-27T11:03:48.081Z"
              |    },
              |    {
              |      "description": "some other description",
              |      "usage": {
              |        "daily": 10,
              |        "monthly": 100
              |      },
              |      "maxResultSetSize": 20,
              |      "startDate": "2019-05-27T17:03:48.081Z"
              |    }
              |  ]
              |}""".stripMargin)
        }
      }

      "return 409 when updated tier tries to override an existing tier" in {
        // given
        tierRepository.create(TierName("a_b"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // when
        val response: RouteTestResult = HttpRequest(
            HttpMethods.POST,
            uri = "/tiers/a_b/configurations",
            entity = HttpEntity(
              MediaTypes.`application/json`,
              s"""{
              |  "description": "some other description",
              |  "usage": {
              |    "daily": 10,
              |    "monthly": 100
              |  },
              |  "maxResultSetSize": 20,
              |  "startDate": "${now.minusSeconds(1)}"
              |}""".stripMargin
            )
          ) ~> sut.updateTierRoute(adminSession)

        // then
        response ~> check {
          status shouldEqual StatusCodes.Conflict
          contentType shouldBe NoContentType
        }
      }

      "get tier" in {
        // given
        HttpRequest(
          HttpMethods.PUT,
          uri = "/tiers/a_b",
          entity = HttpEntity(
            MediaTypes.`application/json`,
            """{
                                                      |  "description": "some description",
                                                      |  "usage": {
                                                      |    "daily": 10,
                                                      |    "monthly": 100
                                                      |  },
                                                      |  "maxResultSetSize": 20
                                                      |}""".stripMargin
          )
        ) ~> sut.createTierRoute(adminSession)

        // when
        val result = Get("/tiers/a_b") ~> sut.getTierRoute(adminSession)

        result ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""{
                                                |  "name": "a_b",
                                                |  "configurations": [
                                                |    {
                                                |      "description": "some description",
                                                |      "usage": {
                                                |        "daily": 10,
                                                |        "monthly": 100
                                                |      },
                                                |      "maxResultSetSize": 20
                                                |    }
                                                |  ]
                                                |}""".stripMargin)
        }
      }

      "get 404 when a given tier was not found" in {
        // when
        val result = Get("/tiers/not_found") ~> sut.getTierRoute(adminSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }

      "get 403 when getting tier when requested by not admin user" in {
        // when
        val result = Get("/tiers/some_id") ~> sut.getTierRoute(userSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.Forbidden
        }
      }

      "get 400 when getting tier with not valid id (without _ which separating tier and subtier)" in {
        // when
        val result = Get("/tiers/id") ~> sut.getTierRoute(userSession)

        // then
        result ~> check {
          status shouldEqual StatusCodes.BadRequest
        }
      }
    }
}
