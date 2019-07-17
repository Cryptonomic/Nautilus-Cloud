package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryTierRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.PermissionDenied
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierConfiguration, TierName}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class TierServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach {

  val tierRepository = new InMemoryTierRepository[Id]()

  val sut = new TierService[Id](tierRepository)

  override protected def afterEach(): Unit = {
    super.afterEach()
    tierRepository.clear()
  }

  "TierService" should {
      "save tier" in {
        // when
        val tier = tierRepository.create(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            monthlyHits = 100,
            dailyHits = 10,
            maxResultSetSize = 20
          )
        )

        // then
        tier.right.value shouldBe Tier(
          name = TierName("shared", "free"),
          configurations = List(
            TierConfiguration(
              description = "shared free",
              monthlyHits = 100,
              dailyHits = 10,
              maxResultSetSize = 20,
              endDate = None
            )
          )
        )
      }

      "get existing tier" in {
        // given
        tierRepository.create(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            monthlyHits = 100,
            dailyHits = 10,
            maxResultSetSize = 20
          )
        )

        // when
        val result = sut.getTier(TierName("shared", "free"))(adminSession).right.value.value

        // then
        result shouldBe Tier(
          name = TierName("shared", "free"),
          configurations = List(
            TierConfiguration(
              description = "shared free",
              monthlyHits = 100,
              dailyHits = 10,
              maxResultSetSize = 20,
              endDate = None
            )
          )
        )
      }

      "get None when there is no existing tier" in {
        // expect
        sut.getTier(TierName("non_existing", "tier"))(adminSession).right.value shouldBe None
      }

      "get PermissionDenied when requesting tier is not an admin" in {
        // expect
        sut.getTier(TierName("shared", "free"))(userSession).left.value shouldBe a[PermissionDenied]
      }
    }
}
