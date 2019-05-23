package tech.cryptonomic.cloud.nautilus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import tech.cryptonomic.cloud.nautilus.model.{DoobieConfig, HttpConfig}
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepoImpl, UserRepoImpl}
import tech.cryptonomic.cloud.nautilus.routes.endpoint.Docs
import tech.cryptonomic.cloud.nautilus.routes.{ApiKeyRoutes, UserRoutes}
import tech.cryptonomic.cloud.nautilus.services.{ApiKeyServiceImpl, UserServiceImpl}
import pureconfig.loadConfig
import pureconfig.generic.auto._


import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object NautilusCloud extends App {

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  lazy val httpConfig = loadConfig[HttpConfig](namespace = "http").toOption.get
  lazy val doobieConfig = loadConfig[DoobieConfig](namespace = "doobie").toOption.get
  lazy val xa = Transactor.fromDriverManager[IO]("org.postgresql.Driver", doobieConfig.url, doobieConfig.user, doobieConfig.password)
  lazy val apiKeysRepo = new ApiKeyRepoImpl(xa)
  lazy val apiKeysService = new ApiKeyServiceImpl[IO](apiKeysRepo)
  lazy val apiKeysRoutes = new ApiKeyRoutes(apiKeysService)
  lazy val userRepo = new UserRepoImpl(xa)
  lazy val userService = new UserServiceImpl[IO](userRepo, apiKeysRepo)
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

  Http().bindAndHandle(route, httpConfig.host, httpConfig.port)

}
