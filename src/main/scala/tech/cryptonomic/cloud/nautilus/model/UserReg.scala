package tech.cryptonomic.cloud.nautilus.model

import java.sql.Timestamp

case class UserReg(
    userName: String,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
