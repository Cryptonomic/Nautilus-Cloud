package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.ResourceService
import tech.cryptonomic.nautilus.cloud.domain.resources.{Resource, ResourceRepository}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class ResourceRoutesTest
  extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with Fixtures
    with MockFactory {


  val resourceRepo = stub[ResourceRepository[IO]]

  val resourceService = new ResourceService(resourceRepo)

  val sut = new ResourceRoutes(resourceService)

  "The resources route" should {
    "return empty list of resources" in {
      (resourceRepo.getResources _).when().returns(IO.pure(List.empty))
      val getRequest = HttpRequest(
        HttpMethods.GET,
        uri = "/resources"
      )
      getRequest ~> sut.listResources ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe "[]"
      }
    }
    "return single resource by id" in {
      val examplerResource = Resource(1, "dev", "development", "tezos", "alphanet")
      (resourceRepo.getResource _).when(1).returns(IO.pure(Some(examplerResource)))
      val getRequest = HttpRequest(
        HttpMethods.GET,
        uri = "/resources/1"
      )
      getRequest ~> sut.getResource ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] should matchJson("""{"resourceid":1,"resourcename":"dev","description":"development","platform":"tezos","network":"alphanet"}""")
      }
    }
    "return 404 when resource does not exists" in {
      (resourceRepo.getResource _).when(1).returns(IO.pure(None))
      val getRequest = HttpRequest(
        HttpMethods.GET,
        uri = "/resources/1"
      )
      getRequest ~> sut.getResource ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
    "create resource and return its Id" in {
      (resourceRepo.createResource _).when(*).returns(IO.pure(1))
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/resources",
        entity = HttpEntity(MediaTypes.`application/json`, """{"resourcename":"dev","description":"development","platform":"tezos","network":"alphanet"}""")
      )
      postRequest ~> sut.createResource ~> check {
        status shouldEqual StatusCodes.Created
        responseAs[String] shouldBe "1"
      }
    }
  }

}
