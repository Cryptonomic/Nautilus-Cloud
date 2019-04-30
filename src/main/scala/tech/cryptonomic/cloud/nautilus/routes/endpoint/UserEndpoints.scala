package tech.cryptonomic.cloud.nautilus.routes.endpoint

import endpoints.algebra
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserReg}
import tech.cryptonomic.cloud.nautilus.routes.endpoint.schemas.UserSchemas

// TODO:
//  users/{user}/usage	  GET	Gets the number of queries used by the given user

/** User relevant endpoints */
trait UserEndpoints extends algebra.Endpoints with algebra.JsonSchemaEntities with UserSchemas {

  /** User creation endpoint definition */
  def createUser: Endpoint[UserReg, Unit] =
    endpoint(
      request = post(url = path / "users", jsonRequest[UserReg]()),
      response = emptyResponse(Some("User created!")),
      tags = List("User")
    )

  /** User update endpoint definition */
  def updateUser: Endpoint[User, Unit] =
    endpoint(
      request = put(url = path / "users", jsonRequest[User]()),
      response = emptyResponse(Some("User updated!")),
      tags = List("User")
    )

  /** User endpoint definition */
  def getUser: Endpoint[Int, Option[User]] =
    endpoint(
      request = get(url = path / "users" / segment[Int]("userId")),
      response = jsonResponse[User]().orNotFound(),
      tags = List("User")
    )

  /** User keys endpoint definition */
  def getUserKeys: Endpoint[Int, List[ApiKey]] =
    endpoint(
      request = get(url = path / "users" / segment[Int]("userId") / "apiKeys"),
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
