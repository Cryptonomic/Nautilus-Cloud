package tech.cryptonomic.nautilus.cloud.domain.user.history

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

case class UserAction(userId: UserId, performedBy: Option[UserId], time: Instant, ip: Option[String], action: String)
