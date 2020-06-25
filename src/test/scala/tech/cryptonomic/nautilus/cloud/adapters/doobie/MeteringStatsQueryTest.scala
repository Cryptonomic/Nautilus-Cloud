package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.AggregatedMeteringStats
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class MeteringStatsQueryTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new MeteringStatsQueries {}
  // check if all queries are valid
  "MeteringStatsRepo" should {
      "check meteringStatsPerUser" in {
        check(sut.meteringStatsPerUser(1))
      }
      "check insertMeteringStats" in {
        check(sut.insertMeteringStats(AggregatedMeteringStats(1, "", 1, Instant.now(), Instant.now())))
      }
      "check lastRecordedIntervalPerUser" in {
        check(sut.lastRecordedIntervalPerUser)
      }
    }

}
