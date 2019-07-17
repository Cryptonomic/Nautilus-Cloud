package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKeyRepository, CreateApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.{AuthenticationConfiguration, AuthenticationProviderRepository, Session}
import tech.cryptonomic.nautilus.cloud.domain.resources.{Resource, ResourceRepository}
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.tier.{Tier, TierRepository}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, User, UserRepository}

import scala.language.higherKinds
import scala.util.Random

/** Authentication service */
class AuthenticationService[F[_]: Monad](
    config: AuthenticationConfiguration,
    authenticationRepository: AuthenticationProviderRepository[F],
    userRepository: UserRepository[F],
    apiKeyRepository: ApiKeyRepository[F],
    resourcesRepository: ResourceRepository[F],
    tiersRepository: TierRepository[F]
) {

  type Result[T] = Either[Throwable, T]

  /* return login url for authentication */
  def loginUrl: String = config.loginUrl

  /* resolve auth code */
  def resolveAuthCode(code: String): F[Result[Session]] =
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .flatMap(getOrCreateUser)
      .map(_.asSession)
      .value

  private def exchangeCodeForAccessToken(code: String) =
    EitherT(authenticationRepository.exchangeCodeForAccessToken(code))

  private def fetchEmail(accessToken: String) = EitherT(authenticationRepository.fetchEmail(accessToken))

  private def getOrCreateUser(email: String): EitherT[F, Throwable, User] = getUserByEmailAddress(email).flatMap {
    case Some(user) => EitherT.rightT(user)
    case None => createUser(email)
  }

  private def getUserByEmailAddress(email: String): EitherT[F, Throwable, Option[User]] =
    EitherT(userRepository.getUserByEmailAddress(email).map(Right(_)))

  private def createApiKey(userId: UserId, resourceId: ResourceId, tierId: Int): F[Option[String]] = {
    val generatedKey = Random.alphanumeric.take(32).mkString

    {
      for {
        _ <- OptionT(userRepository.getUser(userId))
        _ <- OptionT(resourcesRepository.getResource(resourceId))
        tier <- OptionT(tiersRepository.get(tierId))
      } yield {
        val tierConf = tier.configurations.headOption.map(conf => conf.dailyHits -> conf.monthlyHits)
        val (daily, monthly) = tierConf.getOrElse((0,0))
        (apiKeyRepository.putApiKeyUsage(UsageLeft(generatedKey, daily, monthly)),
          apiKeyRepository.putApiKeyForUser(CreateApiKey(generatedKey, resourceId, userId, tierId, Some(Instant.now()), None)))
          .mapN((_,_) => generatedKey)
      }
    }.value.flatMap(_.sequence)
  }

  private def createUser(email: String): EitherT[F, Throwable, User] = {
    val createUser = CreateUser(email, Role.defaultRole, Instant.now(), config.provider)
    EitherT(userRepository.createUser(createUser).flatMap { userIdEither =>
      userIdEither.bitraverse ( t =>
        t.pure[F],
        userId =>
        (createApiKey(userId, Resource.defaultTezosDevAlphanetId, Tier.defaultTierId),
          createApiKey(userId, Resource.defaultTezosProdMainnetId, Tier.defaultTierId)).mapN { (_, _) =>
          createUser.toUser(userId)
        }
      )
    })
  }
}
