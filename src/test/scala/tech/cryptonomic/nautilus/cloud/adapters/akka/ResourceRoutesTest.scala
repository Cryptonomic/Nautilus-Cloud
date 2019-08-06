package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.stephenn.scalatest.jsonassert.JsonMatchers
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.Environment
import tech.cryptonomic.nautilus.cloud.domain.resources.CreateResource
import tech.cryptonomic.nautilus.cloud.tools.DefaultNautilusContextWithInMemoryImplementations

class ResourceRoutesTest
  extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with JsonMatchers
    with BeforeAndAfterEach {


  val context = new DefaultNautilusContextWithInMemoryImplementations
  val resourceRepo = context.resourcesRepository
  val sut = context.resourceRoutes

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
      resourceRepo.createResource(CreateResource("dev", "development", "tezos", "alphanet", Environment.Development))
      val getRequest = HttpRequest(
        HttpMethods.GET,
        uri = "/resources/1"
      )
      //when
      getRequest ~> sut.getResource ~> check {
        //then
        status shouldEqual StatusCodes.OK
        responseAs[String] should matchJson("""{"resourceId":1,"resourceName":"dev","description":"development","platform":"tezos","network":"alphanet","environment":"dev"}""")
      }
    }
    "return 404 when resource does not exist" in {
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
        entity = HttpEntity(MediaTypes.`application/json`, """{"resourceName":"dev","description":"development","platform":"tezos","network":"alphanet","environment":"dev"}""")
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
