package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.tier.{TierConfiguration, TierName}
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class TierQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new TierQueries {}

  // check if all queries are valid
  "TierRepo" should {
      "check creation of a tier" in {
        check(
          sut.createTierQuery(TierName("shared", "free"))
        )
      }
      "check creation of a tier configuration" in {
        check(
          sut.createTierConfigurationQuery(
            TierName("shared", "free"),
            TierConfiguration(
              description = "shared free",
              monthlyHits = 100,
              dailyHits = 10,
              maxResultSetSize = 20,
              Instant.now
            )
          )
        )
      }
      "check getTier" in {
        check(sut.getTiersConfigurationQuery(TierName("shared", "free")))
      }
    }
}
