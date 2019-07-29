package tech.cryptonomic.nautilus.cloud.domain

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.NotAllowedConfigurationOverride
import tech.cryptonomic.nautilus.cloud.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.domain.tier._
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.IdContext

class TierServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach
    with OneInstancePerTest {

  val context = new IdContext
  val tierRepository = context.tiersRepository

  val sut = context.tierService

  "TierService" should {
      "save tier" in {
        // given
        tierRepository.clear()

        // when
        val tier = sut.createTier(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            Usage(
              daily = 10,
              monthly = 100
            ),
            maxResultSetSize = 20
          )
        )(adminSession)

        // then
        tier.right.value.right.value shouldBe Tier(
          tierId = 1,
          name = TierName("shared", "free"),
          configurations = List(
            TierConfiguration(
              description = "shared free",
              Usage(daily = 10, monthly = 100),
              maxResultSetSize = 20,
              startDate = context.now
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
        tierRepository.clear()

        // given
        sut.createTier(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            Usage(
              daily = 10,
              monthly = 100
            ),
            maxResultSetSize = 20
          )
        )(adminSession)

        // when
        val result = sut.getTier(TierName("shared", "free"))(adminSession).right.value.value

        // then
        result shouldBe Tier(
          tierId = 1,
          name = TierName("shared", "free"),
          configurations = List(
            TierConfiguration(
              description = "shared free",
              Usage(daily = 10, monthly = 100),
              maxResultSetSize = 20,
              startDate = context.now
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
        tierRepository.clear()

        // given
        sut.createTier(
          TierName("shared", "free"),
          CreateTier(
            description = "shared free",
            Usage(daily = 10, monthly = 100),
            maxResultSetSize = 20
          )
        )(adminSession)

        // when
        sut.updateTier(
          TierName("shared", "free"),
          UpdateTier(
            description = "shared free",
            usage = Usage(
              daily = 20,
              monthly = 200
            ),
            maxResultSetSize = 40,
            startDate = Some(context.now.plusSeconds(100))
          )
        )(adminSession)

        // then
        sut.getTier(TierName("shared", "free"))(adminSession).right.value.value shouldBe Tier(
          tierId = 1,
          name = TierName("shared", "free"),
          configurations = List(
            TierConfiguration(
              description = "shared free",
              Usage(daily = 10, monthly = 100),
              maxResultSetSize = 20,
              startDate = context.now
            ),
            TierConfiguration(
              description = "shared free",
              Usage(daily = 20, monthly = 200),
              maxResultSetSize = 40,
              startDate = context.now.plusSeconds(100)
            )
          )
        )
      }

      "not update tier when a given startDate is from the past" in {
        // when
        val result = sut.updateTier(
          TierName("shared", "free"),
          exampleUpdateTier.copy(startDate = Some(context.now.minusSeconds(1)))
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
