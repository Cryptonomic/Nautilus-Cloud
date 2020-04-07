package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import akka.http.scaladsl.server.{Directive0, Directive1}
import com.softwaremill.session.SessionOptions.{oneOff, usingCookies}
import com.softwaremill.session._
import tech.cryptonomic.nautilus.cloud.adapters.akka.session.CustomSessionSerializer._
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session

/* Overridden class to enable same-site extension support */
sealed class SessionManager2[T](config: SessionConfig)(implicit sessionEncoder: SessionEncoder[T])
    extends SessionManager(config)(sessionEncoder) { manager =>

  override val clientSessionManager: ClientSessionManager[T] = new ClientSessionManager[T] {
    override def config = manager.config
    override def sessionEncoder = manager.sessionEncoder
    override def nowMillis = manager.nowMillis

    override def createCookieWithValue(value: String) =
      super.createCookieWithValue(value).copy(extension = Some("SameSite=None"))

  }

}

/* wrapper for akka-session */
class SessionOperations(config: SessionConfig) {

  implicit val sessionManager: SessionManager[Session] = new SessionManager2[Session](config)

  private val sessionContinuity: SessionContinuity[Session] = oneOff
  private val sessionTransport: SetSessionTransport = usingCookies

  def requiredSession: Directive1[Session] = SessionDirectives.requiredSession(sessionContinuity, sessionTransport)

  def invalidateSession: Directive0 = SessionDirectives.invalidateSession(sessionContinuity, sessionTransport)

  def setSession(session: Session): Directive0 =
    SessionDirectives.setSession(sessionContinuity, sessionTransport, session)
}
