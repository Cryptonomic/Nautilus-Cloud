package tech.cryptonomic.cloud.nautilus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepoImpl, UserRepoImpl}
import tech.cryptonomic.cloud.nautilus.routes.endpoint.Docs
import tech.cryptonomic.cloud.nautilus.routes.{ApiKeyRoutes, UserRoutes}
import tech.cryptonomic.cloud.nautilus.services.{ApiKeyServiceImpl, UserServiceImpl}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Nautilus extends App {

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5433/nautilus-local",
    "nautilususer",
    "p@ssw0rd"
  )
  lazy val apiKeysRepo = new ApiKeyRepoImpl
  lazy val apiKeysService = new ApiKeyServiceImpl[IO](apiKeysRepo, xa)
  lazy val apiKeysRoutes = new ApiKeyRoutes(apiKeysService)
  lazy val userRepo = new UserRepoImpl
  lazy val userService = new UserServiceImpl[IO](userRepo, apiKeysRepo, xa)
  lazy val userRoutes = new UserRoutes(userService)

  val route: Route =
    apiKeysRoutes.routes ~
        userRoutes.routes ~
        pathPrefix("docs") {
          pathEndOrSingleSlash {
            getFromResource("web/index.html")
          }
        } ~
        pathPrefix("swagger-ui") {
          getFromResourceDirectory("web/swagger-ui/")
        } ~
        Docs.route

  Http().bindAndHandle(route, "0.0.0.0", 1234)

}
