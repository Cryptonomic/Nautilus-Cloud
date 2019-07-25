package tech.cryptonomic.nautilus.cloud.tools

import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.adapters.inmemory._

object DefaultNautilusContext extends NautilusContext

class DefaultNautilusContextWithInMemoryImplementations extends NautilusContext {
  override lazy val apiKeysRepository = new InMemoryApiKeyRepository()
  override lazy val userRepository = new InMemoryUserRepository()
  override lazy val tierRepository = new InMemoryTierRepository()
  override lazy val authRepository = new InMemoryAuthenticationProviderRepository()
  override lazy val resourcesRepository = new InMemoryResourceRepository()
}
