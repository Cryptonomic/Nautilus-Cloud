package tech.cryptonomic.cloud.nautilus.model

import java.sql.Timestamp

//userid	numeric, primary key	sequence auto-increment pk
//username	text, not null	login name
//useremail	text, not null	email
//userrole	text, not null, default('user')	role, probably enum, other values might be acctadmin (corp customers may want control over their sub-accounts), infraadmin (require 2FA)
//registrationdate, not null	timestamp	validity period start
//accountsource	text, nullable	might be enum, values could be web, campaign id, "internal", "manual" (when we ourselves might make accounts for people)
//accountdescription	text, nullable	to go with account source, something we may enter

case class User(
    userId: Long,
    userName: String,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)

case class UserReg(
    userName: String,
    userEmail: String,
    userRole: String,
    registrationDate: Timestamp,
    accountSource: Option[String],
    accountDescription: Option[String]
)
