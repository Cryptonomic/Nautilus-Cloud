package tech.cryptonomic.nautilus.cloud.domain.user

/** Class used in user registration and update */
case class UpdateUser(
    userRole: Role,
    accountDescription: Option[String]
)
