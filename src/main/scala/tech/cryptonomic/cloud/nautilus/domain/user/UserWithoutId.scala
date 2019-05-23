package tech.cryptonomic.cloud.nautilus.domain.user

import java.sql.Timestamp

/** Class used in user registration and update */
case class UserWithoutId(
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
