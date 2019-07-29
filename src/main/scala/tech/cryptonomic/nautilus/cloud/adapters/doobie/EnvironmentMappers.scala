package tech.cryptonomic.nautilus.cloud.adapters.doobie

import doobie.util.{Get, Put}
import tech.cryptonomic.nautilus.cloud.application.domain.apiKey.Environment

/* Mappers for Environment */
trait EnvironmentMappers {
  implicit val environmentGet: Get[Environment] = Get[String].map(Environment.byName)
  implicit val environmentPut: Put[Environment] = Put[String].contramap(_.name)
}
