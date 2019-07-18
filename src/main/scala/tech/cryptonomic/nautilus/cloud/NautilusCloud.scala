package tech.cryptonomic.nautilus.cloud

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

object NautilusCloud extends App with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val context = NautilusContext
  val httpConfig = context.httpConfig
  val routes = context.routes

  logger.info("Nautilus Cloud started on {} at port {}", httpConfig.host, httpConfig.port)
  Http().bindAndHandle(routes.getAll, httpConfig.host, httpConfig.port)
}
