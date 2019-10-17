package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.effect.IO
import cats.implicits._
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.pagination.Pagination
import tech.cryptonomic.nautilus.cloud.domain.user.{Role, UpdateUser}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.InMemoryDatabase

class UserQueriesTest extends WordSpec with Matchers with Fixtures with IOChecker with InMemoryDatabase {

  override def transactor: Transactor[IO] = testTransactor

  val sut = new UserQueries {}

  // check if all queries are valid
  "UserRepo" should {
      "check creation of user" in {
        check(sut.createUserQuery(exampleCreateUser))
      }
      "check updating of user" in {
        check(sut.updateUserQuery(1, UpdateUser(Role.User.some, true.some, "description".some), Instant.now))
      }
      "check deleting of user " in {
        check(sut.deleteUserQuery(1, Instant.now()))
      }
      "check getUser" in {
        check(sut.getUserQuery(0))
      }
      "check getUserByEmail" in {
        check(sut.getUserByEmailQuery("name@domain.com"))
      }
      "check getUsers" in {
        check(
          sut.getUsersQuery(searchCriteria = SearchCriteria(1.some, "name@domain.com".some, "api-key".some))(
            Pagination.allResults
          )
        )
        check(sut.getUsersQuery(SearchCriteria.empty)(Pagination.allResults))
      }
      "check getUsersCount" in {
        check(sut.getUsersCountQuery(SearchCriteria(1.some, "name@domain.com".some, apiKey = "api-key".some)))
        check(sut.getUsersCountQuery(SearchCriteria.empty))
      }
    }
}
