package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

import tech.cryptonomic.cloud.nautilus.domain.security.Session
import tech.cryptonomic.cloud.nautilus.domain.user.{AuthenticationProvider, Role}

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
