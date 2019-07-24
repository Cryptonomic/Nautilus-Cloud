package tech.cryptonomic.nautilus.cloud.domain.apiKey

/** Example representation of the usage per key */
case class UsageLeft(key: String, daily: Int, monthly: Int)
