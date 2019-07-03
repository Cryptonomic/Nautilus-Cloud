package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, Tier, TierConfiguration, TierName}
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, InMemoryDatabase}

class DoobieTierRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with InMemoryDatabase {

  val now: Instant = Instant.now()

  val sut = DefaultNautilusContext.tierRepository

  "ApiKeyRepo" should {
      "save an user" in {
        // when
        val tier = sut.create(TierName("shared", "free"), CreateTier("description", 1, 2, 3)).unsafeRunSync()

        // then
        tier.right.value should equal(
          Tier(TierName("shared", "free"), List(TierConfiguration("description", 1, 2, 3, None)))
        )
      }

      "receive an user" in {
        // given
        sut.create(TierName("shared", "free"), CreateTier("description", 1, 2, 3)).unsafeRunSync()

        // when
        val tier = sut.get(TierName("shared", "free")).unsafeRunSync()

        // then
        tier.value should equal(Tier(TierName("shared", "free"), List(TierConfiguration("description", 1, 2, 3, None))))
      }
    }
}
