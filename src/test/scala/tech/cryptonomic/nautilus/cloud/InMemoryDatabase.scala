package tech.cryptonomic.nautilus.cloud

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}

import scala.concurrent.ExecutionContext
import scala.io.Source

/**
  * Provides access to a test in-memory database initialized with nautilus schema
  */
trait InMemoryDatabase extends BeforeAndAfterAll with BeforeAndAfterEach {
  self: TestSuite =>
  import java.nio.file._

  import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
  import ru.yandex.qatools.embed.postgresql.distribution.Version

  import scala.collection.JavaConverters._

  /** how to name the database schema for the test */
  protected val databaseName = "nautilus-test"
  /** port to use, try to avoid conflicting usage */
  protected val databasePort = 5555
  /** here are temp files for the embedded process, can wipe out if needed */
  protected val cachedRuntimePath = Paths.get("test-nautilus-postgres-path")

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val testTransactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    s"jdbc:postgresql://localhost:$databasePort/$databaseName",
    EmbeddedPostgres.DEFAULT_USER,
    EmbeddedPostgres.DEFAULT_PASSWORD
  )


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
    dbInstance.start(
      EmbeddedPostgres.cachedRuntimeConfig(cachedRuntimePath),
      "localhost",
      databasePort,
      databaseName,
      EmbeddedPostgres.DEFAULT_USER,
      EmbeddedPostgres.DEFAULT_PASSWORD,
      pgInitParams.asJava,
      pgConfigs.asJava)

    Fragment.const(dbSchema).update.run.transact(testTransactor).unsafeRunSync()
  }

  override protected def afterAll(): Unit = {
    dbInstance.stop()
    super.afterAll()
  }

  protected def truncate(): Unit = {
    allTables.map { table =>
      Fragment.const(s"TRUNCATE $table RESTART IDENTITY CASCADE").update.run.transact(testTransactor).unsafeRunSync()
    }
  }
}
