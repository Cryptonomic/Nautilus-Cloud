package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierConfiguration, TierName, Usage}
import tech.cryptonomic.nautilus.cloud.tools.{FixedClock, InMemoryDatabase}

class DoobieTierRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with InMemoryDatabase {

  val now = Instant.now()

  val context: NautilusContext = new NautilusContext {
    override val clock = new FixedClock(now)
  }

  val sut = context.tierRepository

  "DoobieTierRepository" should {
      "save a tier" in {
        // when
        val tier =
          sut.create(TierName("shared", "free"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // then
        tier.right.value should equal(
          Tier(1, TierName("shared", "free"), List(TierConfiguration("description", Usage(1, 2), 3, now)))
        )
      }

      "update a tier" in {
        // given
        sut.create(TierName("shared", "free"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // when
        sut
          .addConfiguration(
            TierName("shared", "free"),
            TierConfiguration("description", Usage(2, 3), 4, now.plusSeconds(1))
          )
          .unsafeRunSync()

        // then
        sut.get(TierName("shared", "free")).unsafeRunSync().value should equal(
          Tier(
            1,
            TierName("shared", "free"),
            List(
              TierConfiguration("description", Usage(1, 2), 3, now),
              TierConfiguration("description", Usage(2, 3), 4, now.plusSeconds(1))
            )
          )
        )
      }

      "not update an user when new configuration start date override previous configurations" in {
        // given
        sut.create(TierName("shared", "free"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // when
        val result = sut
          .addConfiguration(
            TierName("shared", "free"),
            TierConfiguration("description", Usage(2, 3), 4, now.minusSeconds(1))
          )
          .unsafeRunSync()

        // then
        result.left.value shouldBe a[NotAllowedConfigurationOverride]
        sut.get(TierName("shared", "free")).unsafeRunSync().value should equal(
          Tier(
            1,
            TierName("shared", "free"),
            List(
              TierConfiguration("description", Usage(1, 2), 3, now)
            )
          )
        )
      }

      "get DoobieUniqueTierViolationException when saving a duplicated user" in {
        // given
        sut.create(TierName("shared", "free"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // when
        val tier =
          sut.create(TierName("shared", "free"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // then
        tier.left.value shouldBe a[DoobieUniqueTierViolationException]
      }

      "receive an tier" in {
        // given
        sut.create(TierName("shared", "free"), TierConfiguration("description", Usage(1, 2), 3, now)).unsafeRunSync()

        // when
        val tier = sut.get(TierName("shared", "free")).unsafeRunSync()

        // then
        tier.value should equal(
          Tier(1, TierName("shared", "free"), List(TierConfiguration("description", Usage(1, 2), 3, now)))
        )
      }

      "get on when receiving an user which doesn't exist" in {
        // when
        val tier = sut.get(TierName("shared", "free")).unsafeRunSync()

        // then
        tier should equal(None)
      }
    }
}
