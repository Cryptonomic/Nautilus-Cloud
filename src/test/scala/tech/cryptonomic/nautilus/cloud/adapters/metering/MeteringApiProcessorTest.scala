package tech.cryptonomic.nautilus.cloud.adapters.metering

import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.Id
import org.scalatest.{EitherValues, Matchers, OneInstancePerTest, WordSpec}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory.{
  InMemoryApiKeyRepository,
  InMemoryMeteringApiRepository,
  InMemoryMeteringStatsRepository,
  InMemoryUserRepository
}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyStats
import tech.cryptonomic.nautilus.cloud.domain.metering.stats.AggregatedMeteringStats
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

import scala.concurrent.duration._

class MeteringApiProcessorTest extends WordSpec with Matchers with Fixtures with EitherValues with OneInstancePerTest {

  val apiKeyRepository: InMemoryApiKeyRepository[Id] = new InMemoryApiKeyRepository[Id]
  val userRepository: InMemoryUserRepository[Id] = new InMemoryUserRepository[Id](apiKeyRepository)
  val meteringStatsRepository: InMemoryMeteringStatsRepository[Id] = new InMemoryMeteringStatsRepository[Id]
  val meteringApiRepository: InMemoryMeteringApiRepository[Id] = new InMemoryMeteringApiRepository[Id]
  val config = MeteringApiConfig(
    "http",
    "loclalhost",
    1234,
    5.seconds,
    FiniteDuration(5, TimeUnit.MINUTES),
    FiniteDuration(5, TimeUnit.MINUTES),
    "key"
  )

  val sut = new MeteringProcessor[Id](
    meteringStatsRepository,
    meteringApiRepository,
    apiKeyRepository,
    userRepository,
    config
  )

  "MeteringApiProcessor" should {
      "correctly aggregate single stats entry in empty aggregated stats repo" in {
        //given
        apiKeyRepository.add(exampleApiKey.copy(key = "apikey", userId = 1, dateIssued = Some(Instant.now())))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "email@example.com"))
        meteringApiRepository.addApiKeyStats5m(exampleApiKeyStats5m)
        //when
        sut.process()
        //then
        meteringStatsRepository.statsRepository should contain theSameElementsAs List(
          AggregatedMeteringStats(1, "dev", 1, time5m.minusSeconds(config.statsInterval.toSeconds), time5m)
        )
      }
      "correctly aggregate single stats entry when there is an entry for user" in {
        //given
        apiKeyRepository.add(exampleApiKey.copy(key = "apikey", userId = 1, dateIssued = Some(Instant.now())))
        userRepository.createUser(exampleCreateUser.copy(userEmail = "email@example.com"))
        meteringApiRepository.addApiKeyStats5m(
          exampleApiKeyStats5m ::: List(
                ApiKeyStats(time5m.plusSeconds(config.statsInterval.toSeconds), 1, Some("apikey"))
              )
        )
        meteringStatsRepository.insertStats(
          List(AggregatedMeteringStats(1, "dev", 1, time5m.minusSeconds(config.statsInterval.toSeconds), time5m))
        )
        //when
        sut.process()
        //then
        meteringStatsRepository.statsRepository should contain theSameElementsAs List(
          AggregatedMeteringStats(1, "dev", 1, time5m.minusSeconds(config.statsInterval.toSeconds), time5m),
          AggregatedMeteringStats(1, "dev", 1, time5m, time5m.plusSeconds(config.statsInterval.toSeconds))
        )
      }
    }

}
