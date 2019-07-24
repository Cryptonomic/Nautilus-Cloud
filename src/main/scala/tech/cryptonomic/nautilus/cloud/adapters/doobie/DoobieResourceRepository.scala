package tech.cryptonomic.nautilus.cloud.adapters.doobie

import cats.Monad
import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource, ResourceRepository}

import scala.language.higherKinds

/** Doobie resources repository definition*/
class DoobieResourceRepository[F[_]: Monad](transactor: Transactor[F])(
    implicit bracket: Bracket[F, Throwable]
) extends ResourceRepository[F]
    with ResourceQueries {

  /** Creates resource */
  override def createResource(cr: CreateResource): F[ResourceId] =
    insertResource(cr).withUniqueGeneratedKeys[ResourceId]("resourceid").transact(transactor)

  /** Returns all resources */
  override def getResources: F[List[Resource]] =
    listResources.to[List].transact(transactor)

  /** Selects single resource by id */
  override def getResource(resourceId: ResourceId): F[Option[Resource]] =
    selectResource(resourceId).option.transact(transactor)

}
