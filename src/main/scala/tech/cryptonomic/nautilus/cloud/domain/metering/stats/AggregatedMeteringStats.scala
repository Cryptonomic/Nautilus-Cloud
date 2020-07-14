package tech.cryptonomic.nautilus.cloud.domain.metering.stats

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/**
  * Representation of the metering stats in the DB
  * @param userId id of an user
  * @param environment env for which we gather metering stats
  * @param hits how many times API was called
  * @param periodStart Instant representation of period start
  * @param periodEnd Instant representation of period end
  */
case class AggregatedMeteringStats(
    userId: UserId,
    environment: String,
    hits: Int,
    periodStart: Instant,
    periodEnd: Instant
)
