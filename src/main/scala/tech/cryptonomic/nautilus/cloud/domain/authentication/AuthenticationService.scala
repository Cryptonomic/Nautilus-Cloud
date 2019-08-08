package tech.cryptonomic.nautilus.cloud.domain.authentication

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.effect.Clock
import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKeyService
import tech.cryptonomic.nautilus.cloud.domain.tier.TierRepository
import tech.cryptonomic.nautilus.cloud.domain.tools.ClockTool.ExtendedClock
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, User, UserRepository}

import scala.language.higherKinds

/** Authentication service */
class AuthenticationService[F[_]: Monad](
    config: AuthenticationConfiguration,
    authenticationRepository: AuthenticationProviderRepository[F],
    userRepository: UserRepository[F],
    tiersRepository: TierRepository[F],
    apiKeyService: ApiKeyService[F],
    clock: Clock[F]
) {

  type Result[T] = Either[Throwable, T]

  /* return login url for authentication */
  def loginUrl: String = config.loginUrl

  /* resolve auth code */
  def resolveAuthCode(code: String): F[Result[User]] =
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .flatMap(getOrCreateUser)
      .value

  private def exchangeCodeForAccessToken(code: String) =
    EitherT(authenticationRepository.exchangeCodeForAccessToken(code))

  private def fetchEmail(accessToken: String) = EitherT(authenticationRepository.fetchEmail(accessToken))

  private def getOrCreateUser(email: String): EitherT[F, Throwable, User] = getUserByEmailAddress(email).flatMap {
    case Some(user) => EitherT.rightT(user)
    case None => createUser(email)
  }

  private def getUserByEmailAddress(email: String): EitherT[F, Throwable, Option[User]] =
    EitherT.right(userRepository.getUserByEmailAddress(email))

  private def createUser(email: String): EitherT[F, Throwable, User] =
    for {
      defaultTier <- EitherT.right(tiersRepository.getDefault)
      now <- EitherT.right(clock.currentInstant)
      currentUsage = defaultTier.getCurrentUsage(now)
      createUser = CreateUser(email, Role.defaultRole, now, config.provider, defaultTier.tierId)
      userId <- EitherT(userRepository.createUser(createUser))
      _ <- EitherT.right(apiKeyService.initializeApiKeys(userId, currentUsage))
    } yield createUser.toUser(userId)
}
