package tech.cryptonomic.nautilus.cloud.tools

import com.github.tomakehurst.wiremock
import com.github.tomakehurst.wiremock.client.WireMock
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}

trait WireMockServer extends BeforeAndAfterAll with BeforeAndAfterEach with StrictLogging {
  self: TestSuite =>

  private val port = 4235

  val wireMockServer = new wiremock.WireMockServer(port)

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    logger.info(s"Starting wiremock on port $port")
    wireMockServer.start()
    WireMock.configureFor("localhost", port)
    logger.info("Wiremock started successfully")
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()

    super.afterAll()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    wireMockServer.resetAll()
  }
}
