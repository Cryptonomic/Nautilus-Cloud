package services

import java.sql.Timestamp

import cats.Id
import fixtures.Fixtures
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserWithoutId}
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepo, UserRepo}
import tech.cryptonomic.cloud.nautilus.services.UserServiceImpl

class UserServiceTest extends WordSpec with Matchers with Fixtures {

  val apiKeyRepo = new ApiKeyRepo[Id] {
    override def getAllApiKeys: Id[List[ApiKey]] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Id[Boolean] = ???

    override def getUserApiKeys(userId: Int): Id[List[ApiKey]] = List(exampleApiKey)
  }

  val userRepo = new UserRepo[Id] {
    override def createUser(userReg: UserWithoutId): Id[Unit] = ()

    override def updateUser(user: User): Id[Unit] = ()

    override def getUser(userId: Int): Id[Option[User]] = Some(exampleUser)
  }

  val sut = new UserServiceImpl[Id](userRepo, apiKeyRepo)

  "UserService" should {
    "createUser" in {
      sut.createUser(exampleUserWithoutId) shouldBe ()
    }
    "getUser" in {
      sut.getUser(1) shouldBe Some(exampleUser)
    }
    "updateUser" in {
      sut.updateUser(exampleUser) shouldBe ()
    }
    "getUserApiKeys" in {
      sut.getUserApiKeys(0) shouldBe List(exampleApiKey)
    }

  }
}
