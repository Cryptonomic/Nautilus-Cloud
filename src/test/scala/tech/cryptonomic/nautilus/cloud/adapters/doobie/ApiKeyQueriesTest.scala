package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.IO
import doobie.scalatest._
import doobie.util.transactor.Transactor
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, CreateApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class ApiKeyQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new ApiKeyQueries {}

  // check if all queries are valid
  "ApiKeyRepo" should {
    "check getAllApiKeys" in  {
      check(sut.getAllApiKeysQuery)
    }
    "check validation of ApiKey " in {
      check(sut.validateApiKeyQuery(""))
    }
    "check getUserApiKeys" in {
      check(sut.getUserApiKeysQuery(0))
    }
    "check insertApiKey" in {
      check(sut.putApiKey(CreateApiKey("", 0, 0, 0, Instant.now, None)))
    }
    "check getUsageForUser" in {
      check(sut.getUsageForUser(0))
    }
    "check getUsageForKey" in {
      check(sut.getUsageForKey(""))
    }
    "check putUsage" in {
      check(sut.putUsage(UsageLeft("", 0, 0)))
    }
    "check updateUsage" in {
      check(sut.updateUsage(UsageLeft("", 0, 0)))
    }
  }
}
