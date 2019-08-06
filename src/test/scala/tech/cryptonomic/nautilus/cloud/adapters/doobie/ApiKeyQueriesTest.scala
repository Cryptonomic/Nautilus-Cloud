package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.IO
import doobie.scalatest._
import doobie.util.transactor.Transactor
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, CreateApiKey, Environment, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.tier.Usage
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class ApiKeyQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new ApiKeyQueries {}

  // check if all queries are valid
  "ApiKeyRepo" should {
    "check getAllApiKeys" in  {
      check(sut.getAllApiKeysQuery)
    }
    "check getActiveApiKeysQuery" in  {
      check(sut.getActiveApiKeysQuery(1))
    }
    "check validation of ApiKey " in {
      check(sut.validateApiKeyQuery(""))
    }
    "check getUserApiKeys" in {
      check(sut.getUserApiKeysQuery(0))
    }
    "check insertApiKey" in {
      check(sut.putApiKeyQuery(CreateApiKey("", Environment.Development, 0, Instant.now, None)))
    }
    "check getUsageForUser" in {
      check(sut.getUsageForUserQuery(0))
    }
    "check getUsageForKey" in {
      check(sut.getUsageForKeyQuery(""))
    }
    "check putUsage" in {
      check(sut.putUsageQuery(UsageLeft("", Usage.default)))
    }
    "check updateUsage" in {
      check(sut.updateUsageQuery(UsageLeft("", Usage.default)))
    }
    "check invalidateApiKey" in {
      check(sut.invalidateApiKeyQuery(InvalidateApiKey(Environment.Development, 1, Instant.now())))
    }
    "check getKeysForEnvQuery" in {
      check(sut.getKeysForEnvQuery(Environment.Development))
    }
  }
}
