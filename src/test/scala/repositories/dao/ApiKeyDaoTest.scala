package repositories.dao

import cats.effect.IO
import doobie.scalatest._
import doobie.util.transactor.Transactor
import org.scalatest._
import repositories.InMemoryDatabase
import tech.cryptonomic.cloud.nautilus.repositories.dao.ApiKeyDao

class ApiKeyDaoTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new ApiKeyDao {}

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
