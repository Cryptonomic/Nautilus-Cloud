package tech.cryptonomic.nautilus.cloud.domain.user

sealed abstract class AuthenticationProvider(val name: String) extends Product with Serializable

object AuthenticationProvider {
  def byName(name: String): AuthenticationProvider = Seq(Github).find(_.name == name).getOrElse(Undefined)

  case object Github extends AuthenticationProvider("github")
  case object Undefined extends AuthenticationProvider("undefined")
}
