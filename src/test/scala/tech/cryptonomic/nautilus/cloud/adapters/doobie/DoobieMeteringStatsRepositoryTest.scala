package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.util.concurrent.TimeUnit

import org.scalatest.{BeforeAndAfterEach, EitherValues, Matchers, OneInstancePerTest, OptionValues, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.metering.MeteringApiConfig
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.AggregatedMeteringStats
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, InMemoryDatabase}

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

class DoobieMeteringStatsRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with InMemoryDatabase
    with BeforeAndAfterEach
    with Fixtures
    with OneInstancePerTest {

  val context = DefaultNautilusContext
  val sut = context.meteringStatsRepository
  val config = MeteringApiConfig(
    "http",
    "loclalhost",
    1234,
    5.seconds,
    FiniteDuration(5, TimeUnit.MINUTES),
    FiniteDuration(5, TimeUnit.MINUTES),
    "key"
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    applySchemaWithFixtures()
    context.userRepository.createUser(exampleCreateUser).unsafeRunSync()
    context.userRepository.createUser(exampleCreateUser.copy(userEmail = "another@domain.com")).unsafeRunSync()
  }

  "DoobieMeteringStatsRepository" should {
      "insert and read aggregated stats" in {
        // given
        val exampleAggregateMeteringStats = List(
          AggregatedMeteringStats(
            exampleUser.userId,
            "dev",
            1,
            time5m.minusSeconds(config.statsInterval.toSeconds),
            time5m
          )
        )
        sut.insertStats(exampleAggregateMeteringStats).unsafeRunSync()
        // when
        val result = sut.getStatsPerUser(exampleUser.userId)
        // then
        result.unsafeRunSync() should contain theSameElementsAs exampleAggregateMeteringStats
      }

      "not insert duplicated stats" in {
        // given
        val exampleAggregateMeteringStats = List(
          AggregatedMeteringStats(
            exampleUser.userId,
            "dev",
            1,
            time5m.minusSeconds(config.statsInterval.toSeconds),
            time5m
          )
        )
        sut
          .insertStats(exampleAggregateMeteringStats)
          .flatMap(_ => sut.insertStats(exampleAggregateMeteringStats))
          .unsafeRunSync()
        // when
        val result = sut.getStatsPerUser(exampleUser.userId)
        // then
        result.unsafeRunSync() should contain theSameElementsAs exampleAggregateMeteringStats
      }

      "get correct statistics by timestamp" in {
        // given
        val statsInterval = config.statsInterval.toSeconds
        val middleInterval = time5m
        val firstInterval = time5m.minusSeconds(statsInterval)
        val lastInterval = time5m.plusSeconds(statsInterval)
        val exampleMiddleMeteringStats = AggregatedMeteringStats(
          userId = exampleUser.userId,
          environment = "dev",
          hits = 1,
          periodStart = middleInterval.minusSeconds(statsInterval),
          periodEnd = middleInterval
        )
        val exampleFirstMeteringStats = exampleMiddleMeteringStats.copy(
          periodStart = firstInterval.minusSeconds(statsInterval),
          periodEnd = firstInterval
        )
        val exampleLastMeteringStats = exampleMiddleMeteringStats.copy(
          periodStart = lastInterval.minusSeconds(statsInterval),
          periodEnd = lastInterval
        )
        val exampleAggregateMeteringStats = List(
          exampleFirstMeteringStats,
          exampleMiddleMeteringStats,
          exampleLastMeteringStats
        )
        sut.insertStats(exampleAggregateMeteringStats).unsafeRunSync()
        // when
        val result = sut.getLastStats(List(exampleUser.userId))
        // then
        result.unsafeRunSync() should contain theSameElementsAs List(exampleLastMeteringStats)
      }
    }

}
