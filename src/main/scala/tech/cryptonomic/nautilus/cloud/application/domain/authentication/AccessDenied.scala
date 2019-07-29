package tech.cryptonomic.nautilus.cloud.application.domain.authentication

import tech.cryptonomic.nautilus.cloud.application.domain.user.Role

case class AccessDenied(message: String) extends RuntimeException

object AccessDenied {
  def apply(requiredRole: Role, session: Session): AccessDenied =
    new AccessDenied(
      s"""Access denied for ${session.email}. Required role: $requiredRole, given role: ${session.role}"""
    )
}
