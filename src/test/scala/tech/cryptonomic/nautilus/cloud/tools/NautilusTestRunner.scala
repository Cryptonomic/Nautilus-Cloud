package tech.cryptonomic.nautilus.cloud.tools

import java.net.Socket

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import tech.cryptonomic.nautilus.cloud.{NautilusCloud, NautilusContext}

import scala.util.Try

trait NautilusTestRunner extends BeforeAndAfterEach with StrictLogging { self: TestSuite =>

  val nautilusContext: NautilusContext

  private lazy val httpConfig = nautilusContext.httpConfig

  val thread: Thread = new Thread {
    override def run() {
      logger.info("Starting Nautilus Cloud")
      new NautilusCloud(nautilusContext)
    }
  }
  thread.start()

  // wait for a Nautilus to start
  while (!isNautilusRunning) {
    logger.info("Waiting for a Nautilus Cloud to start")
    Thread.sleep(500)
  }

  private def isNautilusRunning: Boolean =
    Try(new Socket(httpConfig.host, httpConfig.port).close()).isSuccess

  override protected def afterEach(): Unit = {
    thread.stop()
    logger.info("Nautilus Cloud stopped successfully")
  }
}
