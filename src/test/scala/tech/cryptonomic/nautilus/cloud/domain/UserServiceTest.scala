package tech.cryptonomic.nautilus.cloud.domain

import cats.Id
import org.scalatest.{EitherValues, Matchers, WordSpec}
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, CreateApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource, ResourceRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User, UserRepository}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class UserServiceTest extends WordSpec with Matchers with Fixtures with EitherValues {

  val apiKeyRepo = new ApiKeyRepository[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = ???

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)

    override def getKeysUsageForUser(userId: Int): Id[List[UsageLeft]] = List(exampleUsageLeft)

    override def getKeyUsage(key: String): Id[Option[UsageLeft]] = Some(exampleUsageLeft)

    override def updateKeyUsage(usage: UsageLeft): Id[Unit] = ()

    override def putApiKeyForUser(apiKey: CreateApiKey): Id[Unit] = ???

    override def putApiKeyUsage(usageLeft: UsageLeft): Id[Unit] = ???
  }

  val userRepo = new UserRepository[Id] {
    override def createUser(userReg: CreateUser): Either[Throwable, UserId] = Right(1)

    override def updateUser(id: UserId, user: UpdateUser): Unit = ()

    override def getUser(userId: UserId): Option[User] = Some(exampleUser)

    override def getUserByEmailAddress(email: String): Id[Option[User]] = Some(exampleUser)
  }

  val resourcesRepo = new ResourceRepository[Id] {
    override def createResource(cr: CreateResource): Id[ResourceId] = ???

    override def getResources: Id[List[Resource]] = ???

    override def getResource(resourceId: ResourceId): Id[Option[Resource]] = ???
  }

  val sut = new UserService[Id](userRepo, apiKeyRepo, resourcesRepo)

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
    "getKeyUsage" in {
      sut.getUserApiKeysUsage(1) shouldBe List(exampleUsageLeft)
    }
  }
}
