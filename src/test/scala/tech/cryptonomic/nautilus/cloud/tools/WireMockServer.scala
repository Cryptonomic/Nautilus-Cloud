package tech.cryptonomic.nautilus.cloud.tools

import com.github.tomakehurst.wiremock
import com.github.tomakehurst.wiremock.client.WireMock
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}

trait WireMockServer extends BeforeAndAfterAll with BeforeAndAfterEach with StrictLogging {
  self: TestSuite =>

  val wireMockServer = new wiremock.WireMockServer(4235)

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    wireMockServer.start()
    WireMock.configureFor("localhost", 4235)
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
