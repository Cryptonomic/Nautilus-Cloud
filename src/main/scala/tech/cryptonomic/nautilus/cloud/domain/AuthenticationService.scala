package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.authentication.{
  AuthenticationConfiguration,
  AuthenticationProviderRepository,
  Session
}
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.{Resource, ResourceRepository}
import tech.cryptonomic.nautilus.cloud.domain.tier.TierRepository
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, User, UserRepository}

import scala.concurrent.duration.MILLISECONDS
import scala.language.higherKinds

/** Authentication service */
class AuthenticationService[F[_]: Monad](
    config: AuthenticationConfiguration,
    authenticationRepository: AuthenticationProviderRepository[F],
    userRepository: UserRepository[F],
    apiKeyRepository: ApiKeyRepository[F],
    resourcesRepository: ResourceRepository[F],
    tiersRepository: TierRepository[F],
    clock: Clock[F],
    apiKeyGenerator: ApiKeyGenerator
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

  private def createUser(email: String): EitherT[F, Throwable, User] = {
    val createUser =
      CreateUser(email, Role.defaultRole, Instant.now(), config.provider)
    EitherT(userRepository.createUser(createUser).flatMap { userIdEither =>
      userIdEither.bitraverse(
        t => t.pure[F],
        userId =>
          tiersRepository.getDefaultTier.flatMap { maybeTier =>
            (
              createApiKey(
                userId,
                Resource.defaultTezosDevAlphanetId,
                maybeTier.get.tierId // there always should be default tier
              ),
              createApiKey(
                userId,
                Resource.defaultTezosProdMainnetId,
                maybeTier.get.tierId // there always should be default tier
              )
            ).mapN { (_, _) =>
              createUser.toUser(userId)
            }
          }
      )
    })
  }

  private def createApiKey(userId: UserId, resourceId: ResourceId, tierId: Int): F[Option[String]] = {
    val generatedKey = apiKeyGenerator.generateKey
    (for {
      _ <- OptionT(resourcesRepository.getResource(resourceId))
      tier <- OptionT(tiersRepository.get(tierId))
    } yield {
      for {
        now <- clock.realTime(MILLISECONDS)
        instantNow = Instant.ofEpochMilli(now)
        dailyMonthly = tier.findValidDailyMonthlyHits(instantNow)
        _ <- apiKeyRepository.putApiKeyForUser(
          CreateApiKey(
            generatedKey,
            resourceId,
            userId,
            tierId,
            instantNow,
            None
          )
        )
        _ <- apiKeyRepository.putApiKeyUsage(
          UsageLeft(generatedKey, dailyMonthly._1, dailyMonthly._2)
        )
      } yield {
        generatedKey
      }
    }).value.flatMap(_.sequence)
  }
}
