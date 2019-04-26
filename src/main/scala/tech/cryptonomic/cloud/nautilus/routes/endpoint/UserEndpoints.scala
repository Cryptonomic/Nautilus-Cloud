package tech.cryptonomic.cloud.nautilus.routes.endpoint

import endpoints.algebra
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserReg}
import tech.cryptonomic.cloud.nautilus.routes.endpoint.schemas.UserSchemas

// TODO:
//  users/{user}/usage	  GET	Gets the number of queries used by the given user
trait UserEndpoints extends algebra.Endpoints with algebra.JsonSchemaEntities with UserSchemas {

  def createUser: Endpoint[UserReg, Unit] =
    endpoint(
      request = post(url = path / "users", jsonRequest[UserReg]()),
      response = emptyResponse(Some("User created!")),
      tags = List("User")
    )

  def updateUser: Endpoint[User, Unit] =
    endpoint(
      request = put(url = path / "users", jsonRequest[User]()),
      response = emptyResponse(Some("User updated!")),
      tags = List("User")
    )

  def getUser: Endpoint[Long, Option[User]] =
    endpoint(
      request = get(url = path / "users" / segment[Long]("userId")),
      response = jsonResponse[User]().orNotFound(),
      tags = List("User")
    )

  def getUserKeys: Endpoint[Long, List[ApiKey]] =
    endpoint(
      request = get(url = path / "users" / segment[Long]("userId") / "apiKeys"),
      response = jsonResponse[List[ApiKey]](),
      tags = List("User")
    )

//  def getQueriesUsage: Endpoint[Long, Option[List[ApiKey]]] =
//    endpoint(
//      request = get(url = path / "users" / segment[Long]("userId") / "usage"),
//      response = jsonResponse[List[Usage]]().orNotFound(),
//      tags = List("User")
//    )
}
