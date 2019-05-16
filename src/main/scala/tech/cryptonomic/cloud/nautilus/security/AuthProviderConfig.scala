package tech.cryptonomic.cloud.nautilus.security

case class AuthProviderConfig(
    clientId: String,
    clientSecret: String,
    accessTokenUrl: String,
    loginUrl: String,
    getUserUrl: String,
    connectionTimeout: Int,
    readTimeout: Int
)
