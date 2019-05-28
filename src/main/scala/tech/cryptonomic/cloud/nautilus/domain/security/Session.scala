package tech.cryptonomic.cloud.nautilus.domain.security

import tech.cryptonomic.cloud.nautilus.domain.user.{AuthenticationProvider, Role}

final case class Session(email: String, provider: AuthenticationProvider, role: Role)
