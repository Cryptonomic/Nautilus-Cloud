package tech.cryptonomic.nautilus.cloud.domain.user

import tech.cryptonomic.cloud.nautilus.domain.user.{AuthenticationProvider, Role}

/** Class used in user registration and update */
case class UpdateUser(
    userEmail: String,
    userRole: Role,
    accountSource: AuthenticationProvider,
    accountDescription: Option[String]
)
