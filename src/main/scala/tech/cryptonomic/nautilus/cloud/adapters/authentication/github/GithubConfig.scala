package tech.cryptonomic.nautilus.cloud.adapters.authentication.github

import scala.concurrent.duration.FiniteDuration

/* github configuration case class */
case class GithubConfig(
    clientId: String,
    clientSecret: String,
    accessTokenUrl: String,
    loginUrl: String,
    emailsUrl: String,
    connectionTimeout: FiniteDuration,
    readTimeout: FiniteDuration
)
