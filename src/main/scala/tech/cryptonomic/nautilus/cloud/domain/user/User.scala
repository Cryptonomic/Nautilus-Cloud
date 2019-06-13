package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/** Class representing User */
case class User(
    userId: UserId,
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    accountDescription: Option[String] = None
) {
  lazy val asSession = Session(userEmail, accountSource, userRole)
}

object User {
  type UserId = Int
}
