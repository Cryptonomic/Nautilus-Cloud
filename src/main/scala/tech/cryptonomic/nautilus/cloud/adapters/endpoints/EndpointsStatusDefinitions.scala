package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import endpoints.algebra.Documentation
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission

/** Utility trait for extending Endpoints functionality */
trait EndpointsStatusDefinitions extends algebra.Responses {

  /** Extension for using Created status code */
  def created[A](response: Response[A], invalidDocs: Documentation): Response[A]

  /** Extension for using Conflict status code */
  def conflict[A](response: Response[A], invalidDocs: Documentation): Response[Either[Throwable, A]]

  /** Extension for using Bad request status code */
  def badRequest[A](response: Response[A], invalidDocs: Documentation): Response[Either[Throwable, A]]

  /** Extension for using Forbidden status code */
  def forbidden[A](response: Response[A], invalidDocs: Documentation): Response[Permission[A]]

  /** Extensions for [[Response]]. */
  implicit class CustomResponseExtensions[A](response: Response[A]) {

    /** syntax for `created` */
    final def withCreatedStatus(notFoundDocs: Documentation = None): Response[A] = created(response, notFoundDocs)

    /** syntax for `conflict` */
    final def orConflict(conflictDocs: Documentation = None): Response[Either[Throwable, A]] = conflict(response, conflictDocs)

    /** syntax for `bad request` */
    final def orBadRequest(conflictDocs: Documentation = None): Response[Either[Throwable, A]] = badRequest(response, conflictDocs)

    /** syntax for `forbidden` */
    final def orForbidden(forbiddenDocs: Documentation = None): Response[Permission[A]] =
      forbidden(response, forbiddenDocs)
  }
}
