package tech.cryptonomic.nautilus.cloud.adapters.inmemory

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource, ResourceRepository}

import scala.language.higherKinds

class InMemoryResourceRepository[F[_]: Monad] extends ResourceRepository[F] {

  private var resources: List[Resource] = List.empty

  /** Creates resource */
  override def createResource(cr: CreateResource): F[ResourceId] = this.synchronized {
    val max = resources.map(_.resourceid).max
    (resources = resources :+ Resource(max, cr.resourcename, cr.description, cr.platform, cr.network)).pure[F].map(_ => max)
  }

  /** Returns all resources */
  override def getResources: F[List[Resource]] = this.synchronized {
    resources.pure[F]
  }

  /** Selects single resource by id */
  override def getResource(resourceId: ResourceId): F[Option[Resource]] = this.synchronized {
    resources.find(_.resourceid == resourceId).pure[F]
  }

  /** Creates default resources */
  override def createDefaultResources: F[List[ResourceId]] = ???
}