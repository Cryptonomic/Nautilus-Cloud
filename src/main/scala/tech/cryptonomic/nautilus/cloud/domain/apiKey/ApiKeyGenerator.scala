package tech.cryptonomic.nautilus.cloud.domain.apiKey
import scala.util.Random

/** Class for generating API keys */
class ApiKeyGenerator {
  /** returns alphanumeric API key with length 32 */
  def generateKey: String = Random.alphanumeric.take(32).mkString
}
