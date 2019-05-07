package services

import java.sql.Timestamp

import cats.Id
import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserRegistration}
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepo, UserRepo}
import tech.cryptonomic.cloud.nautilus.services.UserServiceImpl

class UserServiceTest extends WordSpec with Matchers {

  val exampleApiKey = ApiKey(0, "", 1, 2, 3, None, None)

  val exampleUser = User(1, "someUserName", "email@example.com", "user", new Timestamp(1), None, None)

  val exampleUserRegistration = UserRegistration("someUserName", "email@example.com", "user", new Timestamp(1), None, None)

  val apiKeyRepo = new ApiKeyRepo[Id] {
    override def getAllApiKeys: Id[List[ApiKey]] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Id[Boolean] = ???

    override def getUserApiKeys(userId: Int): Id[List[ApiKey]] = ???
  }

  val userRepo = new UserRepo[Id] {
    override def createUser(userReg: UserRegistration): Id[Unit] = ()

    override def updateUser(user: User): Id[Unit] = ()

    override def getUser(userId: Int): Id[Option[User]] = Some(exampleUser)
  }

  val sut = new UserServiceImpl[Id](userRepo, apiKeyRepo)

  "UserService" should {
    "createUser" in {
      sut.createUser(exampleUserRegistration) shouldBe ()
    }
    "getUser" in {
      sut.getUser(1) shouldBe Some(exampleUser)
    }
    "updateUser" in {
      sut.updateUser(exampleUser) shouldBe ()
    }
    "" in {
      sut.getUserApiKeys(0) shouldBe List()
    }

  }
}
