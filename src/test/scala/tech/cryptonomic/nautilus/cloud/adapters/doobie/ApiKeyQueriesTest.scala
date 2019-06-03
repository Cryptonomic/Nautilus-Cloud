package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.effect.IO
import doobie.scalatest._
import doobie.util.transactor.Transactor
import org.scalatest._
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class ApiKeyQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new ApiKeyQueries {}

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
  }
}
