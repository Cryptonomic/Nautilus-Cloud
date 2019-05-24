package tech.cryptonomic.nautilus.cloud.model

import java.sql.Timestamp

/** Class representing User */
case class User(
    userId: Int,
    userName: String,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
