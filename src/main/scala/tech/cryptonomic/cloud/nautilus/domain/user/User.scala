package tech.cryptonomic.cloud.nautilus.domain.user

import java.time.Instant

import tech.cryptonomic.cloud.nautilus.domain.security.Session

/** Class representing User */
case class User(
    userId: Int,
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    accountDescription: Option[String] = None
) {
  def asSession = Session(userEmail, accountSource, userRole)
}
