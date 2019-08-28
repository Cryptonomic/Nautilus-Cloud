package tech.cryptonomic.nautilus.cloud.domain.apiKey
import java.util.UUID.randomUUID

/** Class for generating API keys */
class ApiKeyGenerator {

  /** returns random UUID */
  def generateKey: String = randomUUID().toString
}
