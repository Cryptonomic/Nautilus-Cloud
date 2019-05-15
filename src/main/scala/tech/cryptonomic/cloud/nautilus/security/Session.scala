package tech.cryptonomic.cloud.nautilus.security

import com.softwaremill.session.{MultiValueSessionSerializer, SessionSerializer}

import scala.util.Try

sealed trait Provider

object Provider {
  def fromString(name: String): Option[Provider] = Seq(Github).find(_.toString == name)
}

case object Github extends Provider

case class Session(provider: Provider, email: String)

object Session {
  implicit def serializer: SessionSerializer[Session, String] = new MultiValueSessionSerializer(serialize, deserialize)

  private def deserialize: Map[String, String] => Try[Session] =
    map => Try(Session(Provider.fromString(map("provider")).get, map("email")))

  private def serialize: Session => Map[String, String] =
    session => Map("provider" -> session.provider.toString, "email" -> session.email)
}
