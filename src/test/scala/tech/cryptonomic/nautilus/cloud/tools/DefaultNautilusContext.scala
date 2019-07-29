package tech.cryptonomic.nautilus.cloud.tools

import java.time.Instant

import cats.Id
import com.softwaremill.macwire._
import pureconfig.generic.auto.exportReader
import pureconfig.loadConfig
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.{GithubAuthenticationConfiguration, GithubConfig}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory._
import tech.cryptonomic.nautilus.cloud.domain.{ApiKeyService, AuthenticationService, TierService}

object DefaultNautilusContext extends NautilusContext

class DefaultNautilusContextWithInMemoryImplementations extends NautilusContext {
  override lazy val apiKeysRepository = new InMemoryApiKeyRepository()
  override lazy val userRepository = new InMemoryUserRepository()
  override lazy val tierRepository = new InMemoryTierRepository()
  override lazy val authRepository = new InMemoryAuthenticationProviderRepository()
  override lazy val resourcesRepository = new InMemoryResourceRepository()
}

class IdContext {
  lazy val githubConfig = loadConfig[GithubConfig](namespace = "security.auth.github").toOption.get
  lazy val authConfig = wire[GithubAuthenticationConfiguration]

  lazy val now = Instant.now()
  lazy val clock = new FixedClock[Id](now)

  lazy val apiKeyGenerator = new FixedApiKeyGenerator()
  lazy val authRepository = new InMemoryAuthenticationProviderRepository()
  lazy val userRepository = new InMemoryUserRepository()
  lazy val apiKeyRepository = new InMemoryApiKeyRepository()
  lazy val tiersRepository = new InMemoryTierRepository()

  lazy val apiKeyService = wire[ApiKeyService[Id]]
  lazy val authenticationService = wire[AuthenticationService[Id]]
  lazy val tierService = wire[TierService[Id]]
}
