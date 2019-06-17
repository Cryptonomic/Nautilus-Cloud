package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import endpoints.algebra.Documentation
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission

/** Utility trait for extending Endpoints functionality */
trait EndpointsStatusDefinitions extends algebra.Responses {

  /** Extension for using Created status code */
  def created[A](response: Response[A], invalidDocs: Documentation): Response[A]

  /** Extension for using Conflict status code */
  def conflict[A](response: Response[A], invalidDocs: Documentation): Response[Option[A]]

  /** Extension for using Conflict status code */
  def forbidden[A](response: Response[A], invalidDocs: Documentation): Response[Permission[A]]

  /** Extensions for [[Response]]. */
  implicit class CustomResponseExtensions[A](response: Response[A]) {

    /** syntax for `created` */
    final def withCreatedStatus(notFoundDocs: Documentation = None): Response[A] = created(response, notFoundDocs)

    /** syntax for `conflict` */
    final def orConflict(conflictDocs: Documentation = None): Response[Option[A]] = conflict(response, conflictDocs)

    /** syntax for `conflict` */
    final def orForbidden(forbiddenDocs: Documentation = None): Response[Permission[A]] =
      forbidden(response, forbiddenDocs)
  }
}
