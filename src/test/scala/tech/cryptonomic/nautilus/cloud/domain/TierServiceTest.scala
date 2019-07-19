package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Id
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.NotAllowedConfigurationOverride
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.InMemoryTierRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.domain.tier._
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.OneSecondClock

class TierServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach {

  val now = Instant.parse("2019-07-03T16:22:24.971Z")

  val clock = new OneSecondClock(now)

  val tierRepository = new InMemoryTierRepository[Id]()

  val sut = new TierService[Id](tierRepository, clock)

  override protected def afterEach(): Unit = {
    super.afterEach()
    tierRepository.clear()
    clock.reset()
  }

  "TierService" should {
      "save tier" in {
        // when
        val tier = sut.createTier(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            monthlyHits = 100,
            dailyHits = 10,
            maxResultSetSize = 20
          )
        )(adminSession)

        // then
        tier.right.value.right.value shouldBe Tier(
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
        sut.createTier(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            monthlyHits = 100,
            dailyHits = 10,
            maxResultSetSize = 20
          )
        )(adminSession)

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
        sut.createTier(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            monthlyHits = 100,
            dailyHits = 10,
            maxResultSetSize = 20,
          )
        )(adminSession)

        // when
        sut.updateTier(
          TierName("shared", "free"),
          UpdateTier(
            description = "shared free",
            monthlyHits = 200,
            dailyHits = 20,
            maxResultSetSize = 40,
            startDate = Some(now.plusSeconds(100))
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
              startDate = now.plusSeconds(100)
            )
          )
        )
      }

      "not update tier when a given startDate is from the past" in {
        // when
        val result = sut.updateTier(
          TierName("shared", "free"),
          exampleUpdateTier.copy(startDate = Some(now.minusSeconds(1)))
        )(adminSession)

        // then
        result.right.value.left.value shouldBe a[NotAllowedConfigurationOverride]
      }

      "get AccessDenied when user updating tier is not an admin" in {
        // expect
        sut.updateTier(TierName("shared", "free"), exampleUpdateTier)(userSession).left.value shouldBe a[AccessDenied]
      }
    }
}
