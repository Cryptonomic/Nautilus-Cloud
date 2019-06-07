package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest.{Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.{User, UserRepository, UserWithoutId}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class UserServiceTest extends WordSpec with Matchers with Fixtures {

  val apiKeyRepo = new ApiKeyRepository[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = ???

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)

    override def getKeysUsageForUser(userId: Int): Id[List[UsageLeft]] = List(examleUsageLeft)

    override def getKeyUsage(key: String): Id[Option[UsageLeft]] = Some(examleUsageLeft)

    override def updateKeyUsage(usage: UsageLeft): Id[Unit] = ()
  }

  val userRepo = new UserRepository[Id] {
    override def createUser(userReg: UserWithoutId): Int = 1

    override def updateUser(user: User): Unit = ()

    override def getUser(userId: Int): Option[User] = Some(exampleUser)
  }

  val sut = new UserService[Id](userRepo, apiKeyRepo)

  "UserService" should {
    "createUser" in {
      sut.createUser(exampleUserWithoutId) shouldBe 1
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
    "getKeyUsage" in {
      sut.getUserApiKeysUsage(1) shouldBe List(examleUsageLeft)
    }
  }
}
