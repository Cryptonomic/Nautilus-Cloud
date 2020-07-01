package tech.cryptonomic.nautilus.cloud.adapters.metering
import scala.concurrent.duration.{Duration, FiniteDuration}

/** Config for metering API */
case class MeteringApiConfig(
    protocol: String,
    host: String,
    port: Int,
    readTimeout: Duration,
    gatherInterval: FiniteDuration,
    statsInterval: FiniteDuration,
    key: String
)
