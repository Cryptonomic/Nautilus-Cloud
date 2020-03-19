package tech.cryptonomic.nautilus.cloud

import cats.effect.{Clock, ContextShift, IO}
import com.softwaremill.macwire._
import com.softwaremill.session.SessionConfig
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.SttpBackendOptions.connectionTimeout
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor.Transactor
import pureconfig.generic.auto.exportReader
import pureconfig.loadConfig
import tech.cryptonomic.nautilus.cloud.adapters.akka.cors.CorsConfig
import tech.cryptonomic.nautilus.cloud.adapters.akka.session.{SessionOperations, SessionRoutes}
import tech.cryptonomic.nautilus.cloud.adapters.akka.{
  ApiKeyRoutes,
  HttpConfig,
  ResourceRoutes,
  Routes,
  TierRoutes,
  UserRoutes
}
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.sttp.SttpGithubAuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.{GithubAuthenticationConfiguration, GithubConfig}
import tech.cryptonomic.nautilus.cloud.adapters.conseil.ConseilConfig
import tech.cryptonomic.nautilus.cloud.adapters.doobie.{
  DoobieApiKeyRepository,
  DoobieConfig,
  DoobieResourceRepository,
  DoobieTierRepository,
  DoobieUserRepository
}
import tech.cryptonomic.nautilus.cloud.adapters.metering.MeteringApiConfig
import tech.cryptonomic.nautilus.cloud.adapters.metering.api.MeteringApiRepository
import tech.cryptonomic.nautilus.cloud.adapters.scalacache.InMemoryRegistrationAttemptRepository
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKeyGenerator, ApiKeyRepository, ApiKeyService}
import tech.cryptonomic.nautilus.cloud.domain.authentication.{
  AuthenticationProviderRepository,
  AuthenticationService,
  RegistrationAttemptConfiguration,
  RegistrationAttemptIdGenerator,
  RegistrationAttemptRepository
}
import tech.cryptonomic.nautilus.cloud.domain.resources.{ResourceRepository, ResourceService}
import tech.cryptonomic.nautilus.cloud.domain.tier.{TierRepository, TierService}
import tech.cryptonomic.nautilus.cloud.domain.user.{UserRepository, UserService}
import tech.cryptonomic.nautilus.cloud.application.{
  ApiKeyApplication,
  AuthenticationApplication,
  ResourceApplication,
  TierApplication,
  UserApplication
}
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApi

import scala.concurrent.ExecutionContext

trait NautilusContext extends StrictLogging {

  logger.info("Starting to initialize application config")

  implicit val clock = Clock.create[IO]

  lazy val corsConfig = loadConfig[CorsConfig](namespace = "akka.http.cors").toOption.get
  lazy val githubConfig = loadConfig[GithubConfig](namespace = "security.auth.github").toOption.get
  lazy val doobieConfig = loadConfig[DoobieConfig](namespace = "doobie").toOption.get
  lazy val httpConfig = loadConfig[HttpConfig](namespace = "http").toOption.get
  lazy val conseilConfig = loadConfig[ConseilConfig](namespace = "conseil").toOption.get
  lazy val registrationAttemptConfiguration =
    loadConfig[RegistrationAttemptConfiguration](namespace = "registration-attempt").toOption.get
  lazy val meteringApiConfig = loadConfig[MeteringApiConfig](namespace = "metering-api").toOption.get

  implicit lazy val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit lazy val sttpBackend: SttpBackend[IO, Nothing] =
    AsyncHttpClientCatsBackend[IO](connectionTimeout(githubConfig.connectionTimeout))

  lazy val transactor: Transactor[IO] =
    Transactor.fromDriverManager[IO](doobieConfig.driver, doobieConfig.url, doobieConfig.user, doobieConfig.password)

  lazy val sessionOperations = new SessionOperations(SessionConfig.fromConfig())

  lazy val authConfig = wire[GithubAuthenticationConfiguration]
  lazy val apiKeyGenerator = wire[ApiKeyGenerator]
  lazy val registrationAttemptIdGenerator = wire[RegistrationAttemptIdGenerator]

  lazy val apiKeyRepository: ApiKeyRepository[IO] = wire[DoobieApiKeyRepository[IO]]
  lazy val userRepository: UserRepository[IO] = wire[DoobieUserRepository[IO]]
  lazy val tierRepository: TierRepository[IO] = wire[DoobieTierRepository[IO]]
  lazy val authRepository: AuthenticationProviderRepository[IO] = wire[SttpGithubAuthenticationProviderRepository[IO]]
  lazy val resourcesRepository: ResourceRepository[IO] = wire[DoobieResourceRepository[IO]]
  lazy val registrationAttemptRepository: RegistrationAttemptRepository[IO] =
    wire[InMemoryRegistrationAttemptRepository[IO]]
  lazy val meteringApiRepository: MeteringApi[IO] = wire[MeteringApiRepository[IO]]

  lazy val authenticationService = wire[AuthenticationService[IO]]
  lazy val apiKeysService = wire[ApiKeyService[IO]]
  lazy val tierService = wire[TierService[IO]]
  lazy val userService = wire[UserService[IO]]
  lazy val resourceService = wire[ResourceService[IO]]

  lazy val authenticationApplication = wire[AuthenticationApplication[IO]]
  lazy val apiKeysApplication = wire[ApiKeyApplication[IO]]
  lazy val tierApplication = wire[TierApplication[IO]]
  lazy val userApplication = wire[UserApplication[IO]]
  lazy val resourceApplication = wire[ResourceApplication[IO]]

  lazy val apiKeysRoutes = wire[ApiKeyRoutes]
  lazy val userRoutes = wire[UserRoutes]
  lazy val tierRoutes = wire[TierRoutes]
  lazy val sessionRoutes = wire[SessionRoutes]
  lazy val resourceRoutes = wire[ResourceRoutes]
  lazy val routes = wire[Routes]

  logger.info("Application config initialized successfully")
}
