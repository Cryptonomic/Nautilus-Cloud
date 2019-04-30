package tech.cryptonomic.cloud.nautilus.model

import java.sql.Timestamp

case class User(
    userId: Int,
    userName: String,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
