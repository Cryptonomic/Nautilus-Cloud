package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource}
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class DoobieResourceRepositoryTest extends WordSpec
  with Matchers
  with EitherValues
  with OptionValues
  with InMemoryDatabase
  with OneInstancePerTest {

  val now: Instant = Instant.now()

  val sut = NautilusContext.resourcesRepository

  "Resource repo" should {
    "save and receive resource" in {
      // when
      val id =
        sut.createResource(CreateResource("dev", "development", "tezos", "alphanet")).unsafeRunSync()

      // then
      id should equal(5) // 4 resources are being created by default

      // when
      val fetchedResource = sut.getResource(5).unsafeRunSync()

      // then
      fetchedResource.value should equal(Resource(5, "dev", "development", "tezos", "alphanet"))
    }

    "save and receive multiple resources" in {
      // when
      val firstId =
        sut.createResource(CreateResource("dev", "development", "tezos", "alphanet")).unsafeRunSync()

      // then
      firstId should equal(1)

      // when
      val secondId =
        sut.createResource(CreateResource("dev", "development", "tezos", "mainnet")).unsafeRunSync()

      // then
      secondId should equal(2)

      // when
      val fetchedResources = sut.getResources.unsafeRunSync()

      // then
      fetchedResources should contain theSameElementsAs List(Resource(1, "dev", "development", "tezos", "alphanet"), Resource(2, "dev", "development", "tezos", "mainnet"))
    }
  }

}
