package tech.cryptonomic.nautilus.cloud.domain.authentication

import java.time.Instant

import io.circe.Json
import tech.cryptonomic.nautilus.cloud.domain.authentication.HeaderType.HeaderType
import tech.cryptonomic.nautilus.cloud.domain.authentication.RegistrationAttempt.RegistrationAttemptId
import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, CreateUser, Role, User}

case class RegistrationAttempt(
    id: String,
    userEmail: String,
    registrationDate: Instant,
    accountSource: AuthenticationProvider
) extends Product
    with Serializable {
  def toCreateUser(
      confirmRegistration: RegistrationConfirmation,
      authenticationProvider: AuthenticationProvider,
      tierId: TierId
  ) =
    CreateUser(
      userEmail,
      Role.defaultRole,
      registrationDate,
      accountSource,
      tierId,
      confirmRegistration.tosAccepted,
      confirmRegistration.newsletterAccepted,
      confirmRegistration.ipAddress
    )
}

object RegistrationAttempt {
  type RegistrationAttemptId = String
}

object HeaderType extends Enumeration {

  type HeaderType = Value
  val REGISTRATION, REGISTERED = Value
}

final case class RegistrationConfirmation(
    registrationAttemptId: RegistrationAttemptId,
    ipAddress: Option[String] = None,
    tosAccepted: Boolean = false,
    newsletterAccepted: Boolean = false
)

final case class InitRequest(code: String)

sealed trait InitResponsePayload

final case class InitResponse(header: HeaderType, payload: InitResponsePayload)

final case class UserResponse(
    userId: UserId,
    userEmail: String,
    userRole: String,
    registrationDate: Instant,
    accountSource: String
) extends InitResponsePayload

final case class RegistrationAttemptResponse(
    registrationAttemptId: RegistrationAttemptId
) extends InitResponsePayload

object UserResponse {
  def apply(user: User): UserResponse =
    new UserResponse(user.userId, user.userEmail, user.userRole.name, user.registrationDate, user.accountSource.name)
}

object InitResponseEncoders {
  import io.circe.generic.extras.semiauto._
  import io.circe.generic.extras.defaults.defaultGenericConfiguration
  import io.circe.Encoder

  implicit lazy val initResponseEncoder: Encoder[InitResponse] = deriveEncoder
  implicit lazy val initResponsePayloadEncoder: Encoder[InitResponsePayload] = {
    case ur: UserResponse => userResponseEncoder(ur)
    case RegistrationAttemptResponse(registrationAttemptId) =>
      Json.obj("registrationAttemptId" -> Json.fromString(registrationAttemptId))
  }
  implicit lazy val userResponseEncoder: Encoder[UserResponse] = deriveEncoder
  implicit lazy val registrationAttemptResponseEncoder: Encoder[RegistrationAttemptResponse] = deriveEncoder
  implicit lazy val instantEncoder: Encoder[Instant] = Encoder.encodeString.contramap[Instant](_.toString)
  implicit lazy val headerTypeEncoder: Encoder[HeaderType] = Encoder.enumEncoder(HeaderType)
}
