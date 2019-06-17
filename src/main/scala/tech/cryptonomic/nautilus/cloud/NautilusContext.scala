package tech.cryptonomic.nautilus.cloud

import cats.effect.{ContextShift, IO}
import com.softwaremill.macwire._
import com.softwaremill.session.SessionConfig
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.SttpBackendOptions.connectionTimeout
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor.Transactor
import pureconfig.generic.auto.exportReader
import pureconfig.loadConfig
import tech.cryptonomic.nautilus.cloud.adapters.akka.session.{SessionOperations, SessionRoutes}
import tech.cryptonomic.nautilus.cloud.adapters.akka.{ApiKeyRoutes, HttpConfig, Routes, UserRoutes}
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.sttp.SttpGithubAuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.{GithubAuthenticationConfiguration, GithubConfig}
import tech.cryptonomic.nautilus.cloud.adapters.doobie.{DoobieApiKeyRepository, DoobieConfig, DoobieUserRepository}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService
import tech.cryptonomic.nautilus.cloud.domain.{ApiKeyService, AuthenticationService, UserService}

import scala.concurrent.ExecutionContext

object NautilusContext extends StrictLogging {

  logger.info("Starting to initialize application config")

  lazy val githubConfig = loadConfig[GithubConfig](namespace = "security.auth.github").toOption.get
  lazy val doobieConfig = loadConfig[DoobieConfig](namespace = "doobie").toOption.get
  lazy val httpConfig = loadConfig[HttpConfig](namespace = "http").toOption.get

  implicit lazy val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit lazy val sttpBackend: SttpBackend[IO, Nothing] =
    AsyncHttpClientCatsBackend[IO](connectionTimeout(githubConfig.connectionTimeout))

  lazy val transactor: Transactor[IO] =
    Transactor.fromDriverManager[IO](doobieConfig.driver, doobieConfig.url, doobieConfig.user, doobieConfig.password)

  lazy val sessionOperations = new SessionOperations(SessionConfig.fromConfig())

  lazy val authConfig = wire[GithubAuthenticationConfiguration]

  lazy val apiKeysRepository = wire[DoobieApiKeyRepository[IO]]
  lazy val userRepository = wire[DoobieUserRepository[IO]]
  lazy val authRepository = wire[SttpGithubAuthenticationProviderRepository[IO]]

  lazy val authenticationService = wire[AuthenticationService[IO]]
  lazy val apiKeysService = wire[ApiKeyService[IO]]
  lazy val userService = wire[UserService[IO]]

  lazy val apiKeysRoutes = wire[ApiKeyRoutes]
  lazy val userRoutes = wire[UserRoutes]
  lazy val sessionRoutes = wire[SessionRoutes]
  lazy val routes = wire[Routes]

  logger.info("Application config initialized successfully")
}
