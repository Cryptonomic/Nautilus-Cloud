package tech.cryptonomic.nautilus.cloud.tools

import java.nio.file._

import com.typesafe.scalalogging.StrictLogging
import doobie.implicits._
import doobie.util.fragment.Fragment
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import ru.yandex.qatools.embed.postgresql.distribution.Version
import tech.cryptonomic.nautilus.cloud.NautilusContext

import scala.collection.JavaConverters._
import scala.io.Source

/**
  * Provides access to a test in-memory database initialized with nautilus schema
  */
trait InMemoryDatabase extends BeforeAndAfterAll with BeforeAndAfterEach with StrictLogging {
  self: TestSuite =>

  /** here are temp files for the embedded process, can wipe out if needed */
  protected val cachedRuntimePath = Paths.get("test-nautilus-postgres-path")

  val context = NautilusContext
  val testTransactor = context.transactor
  val config = context.doobieConfig

  /* turns off anti-corruption guarantees settings that will improve performance on testing
   * override to change or add test-specific settings
   */
  protected val pgInitParams = List("--nosync", "--lc-collate=C")

  /* turns off anti-corruption guarantees settings that will improve performance on testing
   * override to change or add test-specific settings
   */
  protected val pgConfigs = List("-c", "full_page_writes=off")

  lazy val dbInstance = new EmbeddedPostgres(Version.V9_5_15)

  protected val dbSchema = Source.fromFile("./doc/nautilus.sql").getLines().mkString("\n")

  protected val allTables = List(
    "users",
    "tiers",
    "api_keys",
    "resources"
  )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    logger.info(s"Starting embedded PostgreSQL on port ${config.port}")
    dbInstance.start(
      EmbeddedPostgres.cachedRuntimeConfig(cachedRuntimePath),
      config.host,
      config.port,
      config.databaseName,
      config.user,
      config.password,
      pgInitParams.asJava,
      pgConfigs.asJava)

    Fragment.const(dbSchema).update.run.transact(testTransactor).unsafeRunSync()
    logger.info("Embedded PostgreSQL started successfully")
  }

  override protected def afterAll(): Unit = {
    dbInstance.stop()
    super.afterAll()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    allTables.map { table =>
      Fragment.const(s"TRUNCATE $table RESTART IDENTITY CASCADE").update.run.transact(testTransactor).unsafeRunSync()
    }
  }
}
