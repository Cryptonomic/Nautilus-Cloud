package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import cats.implicits._
import io.scalaland.chimney.dsl._

/** Class representing User */
case class User(
    userId: UserId,
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    tosAccepted: Boolean,
    newsletterAccepted: Boolean,
    newsletterAcceptedDate: Option[Instant],
    accountDescription: Option[String] = None
) {
  lazy val asSession = Session(userId, userEmail, accountSource, userRole)
}

object User {
  type UserId = Int
}

/** Class used in user registration */
case class CreateUser(
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    tierId: TierId,
    tosAccepted: Boolean,
    newsletterAccepted: Boolean,
    registrationIp: Option[String] = None,
    accountDescription: Option[String] = None
) extends Product
    with Serializable {

  lazy val newsletterAcceptedDate: Option[Instant] = registrationDate.some.filter(_ => newsletterAccepted)

  def toUser(id: UserId): User =
    this
      .into[User]
      .withFieldConst(_.userId, id)
      .withFieldConst(_.newsletterAcceptedDate, newsletterAcceptedDate)
      .transform
}

/** Class used in user update */
case class UpdateUser(
    userRole: Option[Role] = None,
    newsletterAccepted: Option[Boolean] = None,
    accountDescription: Option[String] = None
) {
  def isEmpty: Boolean = userRole.isEmpty && newsletterAccepted.isEmpty && accountDescription.isEmpty

  def newsletterAcceptedDate(now: => Instant): Option[Option[Instant]] =
    newsletterAccepted.map(it => now.some.filter(_ => it))
}

/** Class used in current user update */
case class UpdateCurrentUser(
    newsletterAccepted: Option[Boolean],
    accountDescription: Option[String] = None
) {
  lazy val updateUser: UpdateUser = this.transformInto[UpdateUser]
}

/** Class used in admin user update */
case class AdminUpdateUser(
    userRole: Option[Role],
    accountDescription: Option[String] = None
) {
  lazy val updateUser: UpdateUser = this.transformInto[UpdateUser]
}
