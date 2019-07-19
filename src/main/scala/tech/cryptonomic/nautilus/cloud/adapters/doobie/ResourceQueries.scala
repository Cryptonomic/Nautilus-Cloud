package tech.cryptonomic.nautilus.cloud.adapters.doobie

import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource}

/** Queries for resources table */
trait ResourceQueries {

  /** Inserts resource */
  def insertResource(createResource: CreateResource): Update0 =
    sql"""INSERT INTO resources (resourcename, description, platform, network, environment)
     VALUES(
      ${createResource.resourceName},
      ${createResource.description},
      ${createResource.platform},
      ${createResource.network},
      ${createResource.environment}
      )""".update


  /** Lists all resources */
  def listResources: Query0[Resource] =
    sql"SELECT resourceid, resourcename, description, platform, network, environment FROM resources".query

  /** Returns single resource by id */
  def selectResource(resourceId: ResourceId): Query0[Resource] =
    sql"SELECT resourceid, resourcename, description, platform, network, environment FROM resources WHERE resourceid = $resourceId".query

}
