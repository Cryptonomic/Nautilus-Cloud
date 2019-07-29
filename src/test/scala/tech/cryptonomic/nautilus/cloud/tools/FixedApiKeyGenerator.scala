package tech.cryptonomic.nautilus.cloud.tools

import java.util.concurrent.atomic.AtomicInteger

import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyGenerator

class FixedApiKeyGenerator extends ApiKeyGenerator {
  private val counter = new AtomicInteger(0)
  override def generateKey: String = s"exampleApiKey${counter.getAndIncrement()}"
}
