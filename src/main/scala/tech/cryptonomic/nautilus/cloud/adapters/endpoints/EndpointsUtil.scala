package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra
import endpoints.algebra.Documentation

/** Utility trait for extending Endpoints functionality */
trait EndpointsUtil extends algebra.Responses {

  /** Extension for using Created status code */
  def created[A](response: Response[A], invalidDocs: Documentation): Response[A]

  /** Extension for using Conflict status code */
  def conflict[A](response: Response[A], invalidDocs: Documentation): Response[Option[A]]

  /** Extensions for [[Response]]. */
  implicit class CustomResponseExtensions[A](response: Response[A]) {

    /** syntax for `created` */
    final def withCreatedStatus(notFoundDocs: Documentation = None): Response[A] = created(response, notFoundDocs)

    /** syntax for `conflict` */
    final def orConflict(conflictDocs: Documentation = None): Response[Option[A]] = conflict(response, conflictDocs)
  }
}
