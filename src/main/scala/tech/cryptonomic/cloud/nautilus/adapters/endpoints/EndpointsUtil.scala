package tech.cryptonomic.cloud.nautilus.adapters.endpoints

import endpoints.algebra
import endpoints.algebra.Documentation

/** Utility trait for extending Endpoints functionality */
trait EndpointsUtil extends algebra.Responses {

  /** Extension for using Created status code */
  def created[A](response: Response[A], invalidDocs: Documentation): Response[A]

  /** Extensions for [[Response]]. */
  implicit class CustomResponseExtensions[A](response: Response[A]) {

    /** syntax for `created` */
    final def withCreatedStatus(notFoundDocs: Documentation = None): Response[A] = created(response, notFoundDocs)
  }
}
