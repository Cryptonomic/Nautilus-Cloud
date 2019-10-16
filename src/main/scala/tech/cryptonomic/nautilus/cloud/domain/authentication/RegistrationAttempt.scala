package tech.cryptonomic.nautilus.cloud.domain.authentication

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, CreateUser, Role}

case class RegistrationAttempt(
    id: String,
    userEmail: String,
    registrationDate: Instant,
    accountSource: AuthenticationProvider
) extends Product
    with Serializable {
  def toCreateUser(authenticationProvider: AuthenticationProvider, tierId: TierId) =
    CreateUser(userEmail, Role.defaultRole, registrationDate, accountSource, tierId)
}

object RegistrationAttempt {
  type RegistrationAttemptId = String
}