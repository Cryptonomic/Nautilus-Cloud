package tech.cryptonomic.cloud.nautilus.adapters.sttp

case class GithubConfig(
    clientId: String,
    clientSecret: String,
    accessTokenUrl: String,
    loginUrl: String,
    getEmailsUrl: String,
    connectionTimeout: Int,
    readTimeout: Int
)
