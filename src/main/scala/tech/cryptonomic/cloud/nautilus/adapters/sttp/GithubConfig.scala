package tech.cryptonomic.cloud.nautilus.adapters.sttp

import scala.concurrent.duration.FiniteDuration

case class GithubConfig(
    clientId: String,
    clientSecret: String,
    accessTokenUrl: String,
    loginUrl: String,
    getEmailsUrl: String,
    connectionTimeout: FiniteDuration,
    readTimeout: FiniteDuration
)
