package tech.cryptonomic.nautilus.cloud.application.domain.user

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.application.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.application.domain.user.User.UserId

/** Class representing User */
case class User(
    userId: UserId,
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    accountDescription: Option[String] = None
) {
  lazy val asSession = Session(userId, userEmail, accountSource, userRole)
}

object User {
  type UserId = Int
}
