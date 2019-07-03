package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Id
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryTierRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.domain.tier._
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.FixedClock

class TierServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach {

  val now = Instant.now()

  val tierRepository = new InMemoryTierRepository[Id](new FixedClock(now))

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
              startDate = now
            )
          )
        )
      }

      "get AccessDenied when user saving a tier is not an admin" in {
        // expect
        sut.createTier(TierName("shared", "free"), exampleCreateTier)(userSession).left.value shouldBe a[AccessDenied]
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
              startDate = now
            )
          )
        )
      }

      "get None when there is no existing tier" in {
        // expect
        sut.getTier(TierName("non_existing", "tier"))(adminSession).right.value shouldBe None
      }

      "get AccessDenied when user requesting tier is not an admin" in {
        // expect
        sut.getTier(TierName("shared", "free"))(userSession).left.value shouldBe a[AccessDenied]
      }

      "update tier" in {
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
        sut.updateTier(
          TierName("shared", "free"),
          UpdateTier(
            description = "shared free",
            monthlyHits = 200,
            dailyHits = 20,
            maxResultSetSize = 40,
            startDate = now
          )
        )(adminSession)

        // then
        sut.getTier(TierName("shared", "free"))(adminSession).right.value.value shouldBe Tier(
          name = TierName("shared", "free"),
          configurations = List(
            TierConfiguration(
              description = "shared free",
              monthlyHits = 100,
              dailyHits = 10,
              maxResultSetSize = 20,
              startDate = now
            ),
            TierConfiguration(
              description = "shared free",
              monthlyHits = 200,
              dailyHits = 20,
              maxResultSetSize = 40,
              startDate = now
            )
          )
        )
      }

      "get AccessDenied when user updating tier is not an admin" in {
        // expect
        sut.updateTier(TierName("shared", "free"), exampleUpdateTier)(userSession).left.value shouldBe a[AccessDenied]
      }
    }
}
