package tech.cryptonomic.nautilus.cloud.domain.authentication

import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider

/* authentication configuration */
trait AuthenticationConfiguration {

  /* return login url for authentication */
  def loginUrl: String

  /* return authentication provider */
  def provider: AuthenticationProvider
}
