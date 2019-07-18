package tech.cryptonomic.nautilus.cloud.tools

import java.nio.file._

import com.typesafe.scalalogging.StrictLogging
import doobie.implicits._
import doobie.util.fragment.Fragment
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import ru.yandex.qatools.embed.postgresql.distribution.Version

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try

/**
  * Provides access to a test in-memory database initialized with nautilus schema
  */
trait InMemoryDatabase extends BeforeAndAfterAll with BeforeAndAfterEach {
  self: TestSuite =>

  val database = InMemoryDatabase
  val testTransactor = database.testTransactor

  override protected def beforeEach(): Unit =
    database.allTables.map { table =>
      Fragment.const(s"TRUNCATE $table RESTART IDENTITY CASCADE").update.run.transact(testTransactor).unsafeRunSync()
    }
}

object InMemoryDatabase extends StrictLogging {

  private val context = DefaultNautilusContext
  private val config = context.doobieConfig

  val testTransactor = context.transactor

  private val allTables = List(
    "users",
    "tiers",
    "api_keys",
    "resources"
  )

  if (isDatabaseRunning)
    logger.info(s"Reusing already running embedded PostgreSQL on port ${config.port}")
  else
    startDatabase()

  private def isDatabaseRunning: Boolean = Try(new java.net.Socket(config.host, config.port).close()).isSuccess

  private def startDatabase(): Unit = {

    /** here are temp files for the embedded process, can wipe out if needed */
    val cachedRuntimePath = Paths.get("test-nautilus-postgres-path")

    /* turns off anti-corruption guarantees settings that will improve performance on testing
     * override to change or add test-specific settings
     */
    val pgInitParams = List("--nosync", "--lc-collate=C")

    /* turns off anti-corruption guarantees settings that will improve performance on testing
     * override to change or add test-specific settings
     */
    val pgConfigs = List("-c", "full_page_writes=off")

    val dbInstance = new EmbeddedPostgres(Version.V9_5_15)

    val dbSchema = Source.fromFile("./doc/nautilus.sql").getLines().mkString("\n")

    logger.info(s"Starting embedded PostgreSQL on port ${config.port}")
    dbInstance.start(
      EmbeddedPostgres.cachedRuntimeConfig(cachedRuntimePath),
      config.host,
      config.port,
      config.databaseName,
      config.user,
      config.password,
      pgInitParams.asJava,
      pgConfigs.asJava
    )

    Fragment.const(dbSchema).update.run.transact(testTransactor).unsafeRunSync()
    logger.info("Embedded PostgreSQL started successfully")

    sys.addShutdownHook {
      dbInstance.stop()
      logger.info("Embedded PostgreSQL stopped successfully")
    }
  }
}
