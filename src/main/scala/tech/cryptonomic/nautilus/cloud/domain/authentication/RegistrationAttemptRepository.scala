package tech.cryptonomic.nautilus.cloud.domain.authentication

import tech.cryptonomic.nautilus.cloud.domain.authentication.RegistrationAttempt.RegistrationAttemptId

import scala.language.higherKinds

trait RegistrationAttemptRepository[F[_]] {

  def save(registrationAttempt: RegistrationAttempt): F[Either[Throwable, Unit]]
  def pop(id: RegistrationAttemptId): F[Either[Throwable, RegistrationAttempt]]
}

/* Registration Attempt Not Found Exception */
final case class RegistrationAttemptNotFoundException(registrationAttemptId: String = "")
    extends Exception(s"Not found registration attempt for $registrationAttemptId")
