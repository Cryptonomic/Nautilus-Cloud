package tech.cryptonomic.nautilus.cloud.domain.user

/** Class used in user registration and update */
case class UpdateUser(
    userEmail: String,
    userRole: Role,
    accountSource: AuthenticationProvider,
    accountDescription: Option[String]
)
