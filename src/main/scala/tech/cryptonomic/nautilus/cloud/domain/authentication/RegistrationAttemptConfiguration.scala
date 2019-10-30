package tech.cryptonomic.nautilus.cloud.domain.authentication

import scala.concurrent.duration.Duration

final case class RegistrationAttemptConfiguration(ttlDuration: Duration)
