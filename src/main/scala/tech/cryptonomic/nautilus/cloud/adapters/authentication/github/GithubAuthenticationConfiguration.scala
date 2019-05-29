package tech.cryptonomic.nautilus.cloud.adapters.authentication.github

import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationConfiguration

/* Github authentication configuration */
case class GithubAuthenticationConfiguration(config: GithubConfig) extends AuthenticationConfiguration {

  private val scopes = List("user:email")

  /* return login url for authentication */
  override def loginUrl: String = config.loginUrl + s"?scope=${scopes.mkString(",")}&client_id=${config.clientId}"

}
