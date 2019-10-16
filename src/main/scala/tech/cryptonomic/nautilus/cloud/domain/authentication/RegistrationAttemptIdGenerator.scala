package tech.cryptonomic.nautilus.cloud.domain.authentication

import java.util.UUID.randomUUID

/** Class for generating registration attempt ids */
class RegistrationAttemptIdGenerator {

  /** returns random UUID */
  def generateId: String = randomUUID().toString
}
