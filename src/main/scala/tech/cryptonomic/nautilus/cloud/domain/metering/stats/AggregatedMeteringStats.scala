package tech.cryptonomic.nautilus.cloud.domain.metering.stats

import java.time.Instant

import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId

/** Representation of the metering stats in the DB */
case class AggregatedMeteringStats(userId: UserId, environment: String, hits: Int, periodStart: Instant, periodEnd: Instant)
