package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import com.softwaremill.session.{MultiValueSessionSerializer, SessionSerializer}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.{AuthenticationProvider, Role}

import scala.util.Try

object CustomSessionSerializer {
  implicit def serializer: SessionSerializer[Session, String] = new MultiValueSessionSerializer(serialize, deserialize)

  private def serialize: Session => Map[String, String] =
    session => Map("provider" -> session.provider.name, "role" -> session.role.name, "email" -> session.email)

  private def deserialize: Map[String, String] => Try[Session] =
    map => Try(Session(map("email"), AuthenticationProvider.byName(map("provider")), Role.byName(map("role"))))
}
