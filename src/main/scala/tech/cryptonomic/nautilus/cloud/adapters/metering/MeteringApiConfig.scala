package tech.cryptonomic.nautilus.cloud.adapters.metering
import scala.concurrent.duration.Duration

/** Config for metering API */
case class MeteringApiConfig(host: String, port: Int, readTimeout: Duration)
