package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest.{EitherValues, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User, UserRepository}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class UserServiceTest extends WordSpec with Matchers with Fixtures with EitherValues {

  val apiKeyRepo = new ApiKeyRepository[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = ???

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)
  }

  val userRepo = new UserRepository[Id] {
    override def createUser(userReg: CreateUser): Either[Throwable, Int] = Right(1)

    override def updateUser(id: Int, user: UpdateUser): Unit = ()

    override def getUser(userId: Int): Option[User] = Some(exampleUser)

    override def getUserByEmailAddress(email: String): Id[Option[User]] = Some(exampleUser)
  }

  val sut = new UserService[Id](userRepo, apiKeyRepo)

  "UserService" should {
    "createUser" in {
      sut.createUser(exampleCreateUser).right.value shouldBe 1
    }
    "getUser" in {
      sut.getUser(1) shouldBe Some(exampleUser)
    }
    "updateUser" in {
      sut.updateUser(1, exampleUpdateUser) shouldBe ()
    }
    "getUserApiKeys" in {
      sut.getUserApiKeys(0) shouldBe List(exampleApiKey)
    }

  }
}
