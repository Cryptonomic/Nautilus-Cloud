package tech.cryptonomic.nautilus.cloud.tools

import java.time.Instant

import cats.Id
import com.softwaremill.macwire._
import pureconfig.generic.auto.exportReader
import pureconfig.loadConfig
import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.{GithubAuthenticationConfiguration, GithubConfig}
import tech.cryptonomic.nautilus.cloud.adapters.inmemory._
import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.ApiKeyService
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.AuthenticationService
import tech.cryptonomic.nautilus.cloud.application.domain.resources.ResourceService
import tech.cryptonomic.nautilus.cloud.application.domain.tier.TierService
import tech.cryptonomic.nautilus.cloud.application.domain.user.UserService
import tech.cryptonomic.nautilus.cloud.application.{ApiKeyApplication, AuthenticationApplication, ResourceApplication, TierApplication, UserApplication}

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
  lazy val resourceRepository = new InMemoryResourceRepository()

  lazy val apiKeyService = wire[ApiKeyService[Id]]
  lazy val authenticationService = wire[AuthenticationService[Id]]
  lazy val tierService = wire[TierService[Id]]
  lazy val userService = wire[UserService[Id]]
  lazy val resourceService = wire[ResourceService[Id]]

  lazy val apiKeyApplication = wire[ApiKeyApplication[Id]]
  lazy val authenticationApplication = wire[AuthenticationApplication[Id]]
  lazy val tierApplication = wire[TierApplication[Id]]
  lazy val userApplication = wire[UserApplication[Id]]
  lazy val resourceApplication = wire[ResourceApplication[Id]]
}
