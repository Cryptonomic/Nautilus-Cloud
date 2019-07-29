package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.Environment
import tech.cryptonomic.nautilus.cloud.application.domain.resources.{CreateResource, Resource}
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, InMemoryDatabase}

class DoobieResourceRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with InMemoryDatabase
    with OneInstancePerTest {

  val now: Instant = Instant.now()

  val sut = DefaultNautilusContext.resourcesRepository

  "Resource repo" should {
    "save and receive resource" in {
      // when
      val id =
        sut.createResource(CreateResource("dev", "development", "tezos", "alphanet", Environment.Development)).unsafeRunSync()

        // then
        id should equal(1)

        // when
        val fetchedResource = sut.getResource(1).unsafeRunSync()

      // then
      fetchedResource.value should equal(Resource(1, "dev", "development", "tezos", "alphanet", Environment.Development))
    }

    "save and receive multiple resources" in {
      // given
      sut.createResource(CreateResource("dev", "development", "tezos", "alphanet", Environment.Development)).unsafeRunSync()
      sut.createResource(CreateResource("dev", "development", "tezos", "mainnet", Environment.Development)).unsafeRunSync()

        // when
        val fetchedResources = sut.getResources.unsafeRunSync()

      // then
      fetchedResources should contain theSameElementsAs List(
        Resource(1, "dev", "development", "tezos", "alphanet", Environment.Development),
        Resource(2, "dev", "development", "tezos", "mainnet", Environment.Development)
      )
    }
  }
}
