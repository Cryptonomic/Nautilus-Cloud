package tech.cryptonomic.nautilus.cloud.adapters.authentication.github

import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationConfiguration
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider

/* Github authentication configuration */
case class GithubAuthenticationConfiguration(config: GithubConfig) extends AuthenticationConfiguration {

  private val scopes = List("user:email")

  /* return login url for authentication */
  override def loginUrl: String = config.loginUrl + s"?scope=${scopes.mkString(",")}&client_id=${config.clientId}"

  /* return authentication provider */
  override def provider: AuthenticationProvider = AuthenticationProvider.Github
}
