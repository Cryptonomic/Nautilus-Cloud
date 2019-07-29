package tech.cryptonomic.nautilus.cloud.application

import java.time.ZonedDateTime

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.doobie.NotAllowedConfigurationOverride
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.AccessDenied
import tech.cryptonomic.nautilus.cloud.application.domain.tier._
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.IdContext

class TierApplicationTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach
    with OneInstancePerTest {

  val context = new IdContext {
    override lazy val now = ZonedDateTime.parse("2019-07-29T15:13:05.136Z").toInstant
  }
  val tierRepository = context.tiersRepository

  val sut = context.tierApplication

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
        val resultValue = result.right.value.left.value
        resultValue shouldBe a[NotAllowedConfigurationOverride]
        resultValue.getMessage should equal(
          "Given time 2019-07-29T15:13:04.136Z is from the past. Current time: 2019-07-29T15:13:05.136Z"
        )
      }

      "get AccessDenied when user updating tier is not an admin" in {
        // when
        val result = sut.updateTier(TierName("shared", "free"), exampleUpdateTier)(userSession)

        // expect
        result.left.value shouldBe a[AccessDenied]
        result.left.value.message should equal(
          "Access denied for email@example.com. Required role: Administrator, given role: User"
        )
      }
    }
}
