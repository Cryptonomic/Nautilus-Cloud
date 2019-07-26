package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryResourceRepository
import tech.cryptonomic.nautilus.cloud.domain.ResourceService
import tech.cryptonomic.nautilus.cloud.domain.resources.CreateResource

class ResourceRoutesTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with BeforeAndAfterEach {

  val resourceRepo = new InMemoryResourceRepository[IO]

  val resourceService = new ResourceService(resourceRepo)

  val sut = new ResourceRoutes(resourceService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    resourceRepo.clear()
  }

  "The resources route" should {
      "return empty list of resources" in {
        //given
        val getRequest = HttpRequest(
          HttpMethods.GET,
          uri = "/resources"
        )
        //when
        getRequest ~> sut.listResources ~> check {
          //then
          status shouldEqual StatusCodes.OK
          responseAs[String] shouldBe "[]"
        }
      }
      "return single resource by id" in {
        //given
        resourceRepo.createResource(CreateResource("dev", "development", "tezos", "alphanet", "dev"))
        val getRequest = HttpRequest(
          HttpMethods.GET,
          uri = "/resources/1"
        )
        //when
        getRequest ~> sut.getResource ~> check {
          //then
          status shouldEqual StatusCodes.OK
          responseAs[String] should matchJson(
            """{"resourceid":1,"resourcename":"dev","description":"development","platform":"tezos","network":"alphanet","environment":"dev"}"""
          )
        }
      }
      "return 404 when resource does not exists" in {
        //given
        val getRequest = HttpRequest(
          HttpMethods.GET,
          uri = "/resources/1"
        )
        //when
        getRequest ~> sut.getResource ~> check {
          //then
          status shouldEqual StatusCodes.NotFound
        }
      }
      "create resource and return its Id" in {
        //given
        val postRequest = HttpRequest(
          HttpMethods.POST,
          uri = "/resources",
          entity = HttpEntity(
            MediaTypes.`application/json`,
            """{"resourceName":"dev","description":"development","platform":"tezos","network":"alphanet","environment":"dev"}"""
          )
        )
        //when
        postRequest ~> sut.createResource ~> check {
          //then
          status shouldEqual StatusCodes.Created
          responseAs[String] shouldBe "1"
        }
      }
    }

}
