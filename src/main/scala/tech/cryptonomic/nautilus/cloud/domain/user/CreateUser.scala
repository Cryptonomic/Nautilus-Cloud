package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

import io.scalaland.chimney.dsl._

/** Class used in user registration and update */
case class CreateUser(
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    accountDescription: Option[String] = None
) {
  def toUser(id: Int): User = this.into[User].withFieldConst(_.userId, id).transform
}
