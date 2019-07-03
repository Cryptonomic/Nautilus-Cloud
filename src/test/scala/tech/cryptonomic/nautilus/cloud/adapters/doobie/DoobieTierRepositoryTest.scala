package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierConfiguration, TierName, UpdateTier}
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
      "save an user" in {
        // when
        val tier = sut.create(TierName("shared", "free"), CreateTier("description", 1, 2, 3)).unsafeRunSync()

        // then
        tier.right.value should equal(
          Tier(TierName("shared", "free"), List(TierConfiguration("description", 1, 2, 3, now)))
        )
      }

      "update an user" in {
        // given
        sut.create(TierName("shared", "free"), CreateTier("description", 1, 2, 3)).unsafeRunSync()

        // when
        sut.update(TierName("shared", "free"), UpdateTier("description", 2, 3, 4, now)).unsafeRunSync()

        // then
        sut.get(TierName("shared", "free")).unsafeRunSync().value should equal(
          Tier(
            TierName("shared", "free"),
            List(
              TierConfiguration("description", 1, 2, 3, now),
              TierConfiguration("description", 2, 3, 4, now)
            )
          )
        )
      }

      "receive an user" in {
        // given
        sut.create(TierName("shared", "free"), CreateTier("description", 1, 2, 3)).unsafeRunSync()

        // when
        val tier = sut.get(TierName("shared", "free")).unsafeRunSync()

        // then
        tier.value should equal(Tier(TierName("shared", "free"), List(TierConfiguration("description", 1, 2, 3, now))))
      }
    }
}
