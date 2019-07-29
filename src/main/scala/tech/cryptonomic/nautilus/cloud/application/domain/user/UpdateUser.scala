package tech.cryptonomic.nautilus.cloud.application.domain.user

/** Class used in user update */
case class UpdateUser(
    userRole: Role,
    accountDescription: Option[String]
)
