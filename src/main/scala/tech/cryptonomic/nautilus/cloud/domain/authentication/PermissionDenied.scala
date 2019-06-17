package tech.cryptonomic.nautilus.cloud.domain.authentication

import tech.cryptonomic.nautilus.cloud.domain.user.Role

case class PermissionDenied(message: String) extends RuntimeException

object PermissionDenied {
  def apply(requiredRole: Role, givenRole: Role): PermissionDenied =
    new PermissionDenied(s"""Access permitted. Required role: $requiredRole, given role: $givenRole""")
}
