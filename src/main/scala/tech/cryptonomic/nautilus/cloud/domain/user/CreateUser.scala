package tech.cryptonomic.nautilus.cloud.domain.user

import java.time.Instant

import io.scalaland.chimney.dsl._
import tech.cryptonomic.nautilus.cloud.domain.tier.Tier.TierId
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/** Class used in user registration and update */
case class CreateUser(
    userEmail: String,
    userRole: Role,
    registrationDate: Instant,
    accountSource: AuthenticationProvider,
    tierId: TierId,
    accountDescription: Option[String] = None
) {
  def toUser(id: UserId): User = this.into[User].withFieldConst(_.userId, id).transform
}
