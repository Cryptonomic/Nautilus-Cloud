package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.Environment
import tech.cryptonomic.nautilus.cloud.domain.resources.CreateResource
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class ResourceQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new ResourceQueries {}

  // check if all queries are valid
  "ResourceQueries" should {
    "check selecting single resource" in {
      check(sut.selectResource(0))
    }
    "check creating resource " in {
      check(sut.insertResource(CreateResource("", "", "", "", Environment.Development)))
    }
    "check listing all resources" in {
      check(sut.listResources)
    }
  }

}
