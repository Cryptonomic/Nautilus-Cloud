package tech.cryptonomic.nautilus.cloud.adapters.akka.session

import akka.http.scaladsl.server.Directives.{pass, reject}
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive0, Directive1}
import com.softwaremill.session.SessionOptions.{oneOff, usingCookies}
import com.softwaremill.session._
import tech.cryptonomic.nautilus.cloud.adapters.akka.session.CustomSessionSerializer._
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.Role

/* wrapper for akka-session */
class SessionOperations(config: SessionConfig) {

  implicit val sessionManager: SessionManager[Session] = new SessionManager[Session](config)

  private val sessionContinuity: SessionContinuity[Session] = oneOff
  private val sessionTransport: SetSessionTransport = usingCookies

  def requiredSession: Directive1[Session] = SessionDirectives.requiredSession(sessionContinuity, sessionTransport)

  def invalidateSession: Directive0 = SessionDirectives.invalidateSession(sessionContinuity, sessionTransport)

  def requiredRole(role: Role): Directive0 = requiredSession.flatMap {
    case session if session.role == role => pass
    case _ => reject(AuthorizationFailedRejection)
  }

  def setSession(session: Session): Directive0 =
    SessionDirectives.setSession(sessionContinuity, sessionTransport, session)
}
