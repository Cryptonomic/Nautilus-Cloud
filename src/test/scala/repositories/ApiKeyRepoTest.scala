package repositories

import cats.effect.IO
import doobie.scalatest._
import doobie.util.transactor.Transactor
import org.scalatest._
import tech.cryptonomic.cloud.nautilus.repositories.ApiKeyRepoImpl

class ApiKeyRepoTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new ApiKeyRepoImpl

  "ApiKeyRepo" should {
    "check getAllApiKeys" in {
      check(sut.getAllApiKeys)
    }
    "check validation of ApiKey " in {
      check(sut.validateApiKey(""))
    }
    "check getUserApiKeys" in {
      check(sut.getUserApiKeys(0))
    }
  }

}