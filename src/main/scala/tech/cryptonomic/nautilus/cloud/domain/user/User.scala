package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import io.scalaland.chimney.dsl._

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

/** Class used in user registration and update */
case class CreateUser(
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    tierId: TierId,
    accountDescription: Option[String] = None
) extends Product
    with Serializable {
  def toUser(id: UserId): User = this.into[User].withFieldConst(_.userId, id).transform
}

/** Class used in user update */
case class UpdateUser(
    userRole: Role,
    accountDescription: Option[String]
)
