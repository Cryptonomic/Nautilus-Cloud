package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryTierRepository
import tech.cryptonomic.nautilus.cloud.domain.TierService
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.JsonMatchers

class TierRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with Fixtures
    with MockFactory {

  val tierRepository = new InMemoryTierRepository[IO]()

  val sut = new TierRoutes(new TierService[IO](tierRepository))

  "The Tier route" should {

      "create tier" in {
        // when
        val request: RouteTestResult = HttpRequest(
            HttpMethods.PUT,
            uri = "/tiers/a_b",
            entity = HttpEntity(
              MediaTypes.`application/json`,
              """{
                                                      |  "description": "some description",
                                                      |  "monthlyHits": 100,
                                                      |  "dailyHits": 10,
                                                      |  "maxResultSetSize": 20
                                                      |}""".stripMargin
            )
          ) ~> sut.createTierRoute(adminSession)

        // then
        request ~> check {
          status shouldEqual StatusCodes.Created
          contentType shouldBe ContentTypes.`application/json`
          responseAs[String] should matchJson("""{
                                                |  "name": "a_b",
                                                |  "configurations": [
                                                |    {
                                                |      "description": "some description",
                                                |      "monthlyHits": 100,
                                                |      "dailyHits": 10,
                                                |      "maxResultSetSize": 20
                                                |    }
                                                |  ]
                                                |}""".stripMargin)
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
                                                      |  "monthlyHits": 100,
                                                      |  "dailyHits": 10,
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
                                                |      "monthlyHits": 100,
                                                |      "dailyHits": 10,
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
