package tech.cryptonomic.nautilus.cloud.domain.apiKey

import tech.cryptonomic.nautilus.cloud.domain.tier.Usage

/** Example representation of the usage per key */
case class UsageLeft(key: String, usage: Usage) {
  val daily: Int = usage.daily
  val monthly: Int = usage.monthly
}
