package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Id
import org.scalatest.BeforeAndAfterEach
import org.scalatest.OptionValues
import org.scalatest.EitherValues
import org.scalatest.Matchers
import org.scalatest.WordSpec
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyRepository
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider
import tech.cryptonomic.nautilus.cloud.domain.user.Role
import tech.cryptonomic.nautilus.cloud.domain.user.CreateUser
import tech.cryptonomic.nautilus.cloud.domain.user.UpdateUser
import tech.cryptonomic.nautilus.cloud.domain.user.User
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures

class UserServiceTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach {

  val apiKeyRepo = new ApiKeyRepository[Id] {
    override def getAllApiKeys: List[ApiKey] = List(exampleApiKey)

    override def validateApiKey(apiKey: String): Boolean = ???

    override def getUserApiKeys(userId: Int): List[ApiKey] = List(exampleApiKey)
  }

  val now = Instant.now

  val userRepository = new InMemoryUserRepository[Id]()

  val sut = new UserService[Id](userRepository, apiKeyRepo)

  override protected def afterEach(): Unit = {
    super.afterEach()
    userRepository.clear()
  }

  "UserService" should {
      "get existing user" in {
        // given
        userRepository.createUser(CreateUser("user@domain.com", Role.Administrator, now, AuthenticationProvider.Github))

        // expect
        sut
          .getUser(adminSession)(1)
          .value shouldBe User(1, "user@domain.com", Role.Administrator, now, AuthenticationProvider.Github, None)
      }

      "updateUser" in {
        // given
        userRepository.createUser(CreateUser("user@domain.com", Role.Administrator, now, AuthenticationProvider.Github))

        // when
        sut.updateUser(adminSession)(1, UpdateUser(Role.User, Some("some description")))

        // then
        sut.getUser(userSession)(1).value shouldBe User(
          1,
          "user@domain.com",
          Role.User,
          now,
          AuthenticationProvider.Github,
          Some("some description")
        )
      }

      "getUserApiKeys" in {
        sut.getUserApiKeys(0) shouldBe List(exampleApiKey)
      }

    }
}
