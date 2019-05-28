package tech.cryptonomic.cloud.nautilus.domain.user

sealed abstract class Role(val name: String) extends Product with Serializable

object Role {
  def byName(name: String): Role = Seq(Administrator).find(_.name == name).getOrElse(User)

  case object User extends Role("user")
  case object Administrator extends Role("admin")
}
