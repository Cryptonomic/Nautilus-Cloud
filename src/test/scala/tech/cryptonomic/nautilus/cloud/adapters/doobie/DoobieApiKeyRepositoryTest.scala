package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, CreateApiKey, Environment}
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
          .putApiKey(CreateApiKey("cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, 1, now, None))
          .unsafeRunSync()

        // when
        val fetchedApiKeys = sut.getUserApiKeys(1).unsafeRunSync()

        // then
        fetchedApiKeys should equal(
          List(ApiKey(1, "cfa60c07-2e5e-4e13-8aef-9c178b1a8bd3", Environment.Development, 1, 1, Some(now), None))
        )
      }
    }
}
