package tech.cryptonomic.nautilus.cloud.domain.authentication

/* authentication configuration */
trait AuthenticationConfiguration {

  /* return login url for authentication */
  def loginUrl: String
}
