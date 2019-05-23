package tech.cryptonomic.cloud.nautilus.domain.user

import java.sql.Timestamp

/** Class representing User */
case class User(
    userId: Int,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
