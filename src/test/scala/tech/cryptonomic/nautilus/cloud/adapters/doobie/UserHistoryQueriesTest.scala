package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.user.history.UserAction
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class UserHistoryQueriesTest extends WordSpec with Matchers with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new UserHistoryQueries {}

  // check if all queries are valid
  "UserHistoryQueries" should {
      "check selectUserHistory" in {
        check(sut.selectUserHistoryActions(1))
      }
      "check insertUserHistory" in {
        check(sut.insertUserHistoryAction(UserAction(1, None, Instant.now(), None, "")))
      }
    }
}
