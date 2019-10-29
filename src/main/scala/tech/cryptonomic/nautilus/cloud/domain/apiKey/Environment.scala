package tech.cryptonomic.nautilus.cloud.domain.apiKey

sealed abstract class Environment(val name: String) extends Product with Serializable

object Environment {
  def byName(name: String): Environment = all.find(_.name == name).get
  def all: List[Environment] = Production :: Development :: Nil

  case object Production extends Environment("prod")
  case object Development extends Environment("dev")
}
