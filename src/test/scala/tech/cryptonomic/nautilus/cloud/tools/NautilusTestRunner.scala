package tech.cryptonomic.nautilus.cloud.tools

import java.net.Socket

import com.typesafe.scalalogging.StrictLogging
import tech.cryptonomic.nautilus.cloud.{NautilusCloud, NautilusContext}

import scala.util.Try

trait NautilusTestRunner extends StrictLogging {

  private val httpConfig = NautilusContext.httpConfig

  private def isNautilusRunning: Boolean =
    Try(new Socket(httpConfig.host, httpConfig.port).close()).isSuccess

  if (isNautilusRunning)
    logger.info("Reusing already running Nautilus Cloud on {} at port {}", httpConfig.host, httpConfig.port)
  else
    NautilusCloud.main(Array.empty)
}
