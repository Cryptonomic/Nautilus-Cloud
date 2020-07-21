package tech.cryptonomic.nautilus.cloud.domain.user.history

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/**
  * Representation of the actions logged for the user
  * @param userId user that change was made for
  * @param performedBy user which performed the change
  * @param time when the changed happened
  * @param ip IP from which the action was performed
  * @param action usually endpoint
  */
case class UserAction(userId: UserId, performedBy: Option[UserId], time: Instant, ip: Option[String], action: String)
