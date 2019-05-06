package tech.cryptonomic.cloud.nautilus.model

import java.sql.Timestamp

/** Class used in user registration */
case class UserRegistration(
    userName: String,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
