package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, CreateApiKey, Environment, RefreshApiKey}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.{DefaultNautilusContext, InMemoryDatabase}

class DoobieApiKeyRepositoryTest
    extends WordSpec
    with Matchers
    with EitherValues
    with OptionValues
    with InMemoryDatabase
    with BeforeAndAfterEach
    with Fixtures
    with OneInstancePerTest {

  val context = DefaultNautilusContext
  val now = Instant.now()
  val sut = context.apiKeysRepository

  override def beforeEach(): Unit = {
    super.beforeEach()

    applySchemaWithFixtures()
    context.userRepository.createUser(exampleCreateUser).unsafeRunSync()
  }

  "ApiKey repo" should {
      "save and receive apiKey" in {
        // when
        sut
          .putApiKey(CreateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, now, None))
          .unsafeRunSync()

        // when
        val fetchedApiKeys = sut.getUserApiKeys(1).unsafeRunSync()

        // then
        fetchedApiKeys should equal(
          List(ApiKey(1, "cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, Some(now), None))
        )
      }

      "fetch all apiKey" in {
        // when
        sut
          .putApiKey(CreateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, now, None))
          .unsafeRunSync()

        // when
        val fetchedApiKeys = sut.getAllApiKeys.unsafeRunSync()

        // then
        fetchedApiKeys should equal(
          List(ApiKey(1, "cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, Some(now), None))
        )
      }

      "validate apiKey" in {
        // given
        sut.validateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3").unsafeRunSync() shouldBe(false)

        // when
        sut
          .putApiKey(CreateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, now, None))
          .unsafeRunSync()

        // then
        sut.validateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3").unsafeRunSync() shouldBe(true)
      }

      "suspended apiKey should not be valid" in {
        // when
        sut
          .putApiKey(CreateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, now, Some(Instant.now())))
          .unsafeRunSync()

        // then
        sut.validateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3").unsafeRunSync() shouldBe(false)
      }

      "refresh apiKey" in {
        // when
        sut
          .putApiKey(CreateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, now, None))
          .unsafeRunSync()

        // when
        sut.updateApiKey(RefreshApiKey(1, Environment.Development, "new-api-key", now.plusSeconds(1))).unsafeRunSync()

        // then
        sut.getUserApiKeys(1).unsafeRunSync() should equal(
          List(
            ApiKey(
              1,
              "cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3",
              Environment.Development,
              1,
              Some(now),
              Some(now.plusSeconds(1))
            ),
            ApiKey(2, "new-api-key", Environment.Development, 1, Some(now.plusSeconds(1)), None)
          )
        )
      }

      "invalidate users api keys" in {
        // when
        sut
          .putApiKey(CreateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, now, None))
          .unsafeRunSync()
        sut
          .putApiKey(CreateApiKey("12506413-8b79-49fd-8d11-e39b49efa24c", Environment.Development, 2, now, None))
          .unsafeRunSync()

        // when
        sut.invalidateApiKeys(userId = 1, Instant.now()).unsafeRunSync()

        // then (only api key for a given user should be invalidated)
        sut.validateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3").unsafeRunSync() shouldBe false
        sut.validateApiKey("12506413-8b79-49fd-8d11-e39b49efa24c").unsafeRunSync() shouldBe true
      }

      "get active apiKeys" in {
        // when
        sut
          .putApiKey(exampleCreateApiKey.copy(key = "non-active-key", userId = 1, dateSuspended = Some(Instant.now())))
          .unsafeRunSync()
        sut
          .putApiKey(exampleCreateApiKey.copy(key = "active-key", userId = 1, dateSuspended = None))
          .unsafeRunSync()

        // when
        val apiKeys = sut.getCurrentActiveApiKeys(1).unsafeRunSync()

        // then
        apiKeys.map(_.key) should equal(List("active-key"))
      }
    }
}
