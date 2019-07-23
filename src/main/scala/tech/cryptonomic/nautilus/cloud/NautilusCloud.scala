package tech.cryptonomic.nautilus.cloud

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

class NautilusCloud(context: NautilusContext) extends StrictLogging {

  implicit val system: ActorSystem = ActorSystem("nautilus-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val httpConfig = context.httpConfig
  val routes = context.routes

  logger.info("Nautilus Cloud started on {} at port {}", httpConfig.host, httpConfig.port)
  Http().bindAndHandle(routes.getAll, httpConfig.host, httpConfig.port)
}

object NautilusCloud extends App {

  override def main(args: Array[String]): Unit =
    new NautilusCloud(new NautilusContext {})

}
