package tech.cryptonomic.nautilus.cloud.adapters.metering
import scala.concurrent.duration.Duration

case class MeteringApiConfig(host: String, port: Int, readTimeout: Duration)
