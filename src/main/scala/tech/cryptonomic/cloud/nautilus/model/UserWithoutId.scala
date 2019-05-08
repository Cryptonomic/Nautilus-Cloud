package tech.cryptonomic.cloud.nautilus.model

import java.sql.Timestamp

/** Class used in user registration and update */
case class UserWithoutId(
    userName: String,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
