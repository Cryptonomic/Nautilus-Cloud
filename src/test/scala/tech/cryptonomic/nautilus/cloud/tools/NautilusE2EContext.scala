package tech.cryptonomic.nautilus.cloud.tools

import tech.cryptonomic.nautilus.cloud.NautilusContext
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyGenerator

/** Custom context for E2E tests which needed to be in a trait to work properly */
trait NautilusE2EContext {
  val nautilusContext: NautilusContext = new NautilusContext {
    override lazy val apiKeyGenerator: ApiKeyGenerator = new FixedApiKeyGenerator
  }
}
