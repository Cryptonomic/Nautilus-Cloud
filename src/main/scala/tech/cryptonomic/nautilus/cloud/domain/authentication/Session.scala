package tech.cryptonomic.nautilus.cloud.domain.authentication

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, Role}

/* case class for storing user's session */
final case class Session(userId: UserId, email: String, provider: AuthenticationProvider, role: Role)
