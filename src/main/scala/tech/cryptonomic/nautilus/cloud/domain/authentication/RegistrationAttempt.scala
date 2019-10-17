package tech.cryptonomic.nautilus.cloud.domain.authentication

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.authentication.RegistrationAttempt.RegistrationAttemptId
import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, CreateUser, Role}

case class RegistrationAttempt(
    id: String,
    userEmail: String,
    registrationDate: Instant,
    accountSource: AuthenticationProvider
) extends Product
    with Serializable {
  def toCreateUser(
      confirmRegistration: ConfirmRegistration,
      authenticationProvider: AuthenticationProvider,
      tierId: TierId,
      ip: Option[String]
  ) =
    CreateUser(
      userEmail,
      Role.defaultRole,
      registrationDate,
      accountSource,
      tierId,
      confirmRegistration.tosAccepted,
      confirmRegistration.newsletterAccepted,
      ip
    )
}

object RegistrationAttempt {
  type RegistrationAttemptId = String
}

final case class ConfirmRegistration(
    registrationAttemptId: RegistrationAttemptId,
    tosAccepted: Boolean = false,
    newsletterAccepted: Boolean = false
)
