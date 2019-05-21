package tech.cryptonomic.cloud.nautilus.infrasctructure

import com.softwaremill.session.{MultiValueSessionSerializer, SessionSerializer}

import scala.util.Try

sealed abstract class Provider(val name: String) extends Product with Serializable

object Provider {
  def fromString(name: String): Option[Provider] = Seq(Github).find(_.name == name)

  case object Github extends Provider("github")
}

final case class Session(provider: Provider, email: String)

object Session {
  implicit def serializer: SessionSerializer[Session, String] = new MultiValueSessionSerializer(serialize, deserialize)

  private def serialize: Session => Map[String, String] =
    session => Map("provider" -> session.provider.name, "email" -> session.email)

  private def deserialize: Map[String, String] => Try[Session] =
    map => Try(Session(Provider.fromString(map("provider")).get, map("email")))
}
