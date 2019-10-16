package tech.cryptonomic.nautilus.cloud.domain.authentication

import cats.Monad
import cats.data.EitherT
import cats.effect.Clock
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKeyGenerator, ApiKeyService}
import tech.cryptonomic.nautilus.cloud.domain.authentication.RegistrationAttempt.RegistrationAttemptId
import tech.cryptonomic.nautilus.cloud.domain.tier.TierRepository
import tech.cryptonomic.nautilus.cloud.domain.tools.ClockTool.ExtendedClock
import tech.cryptonomic.nautilus.cloud.domain.user.{User, UserRepository}

import scala.language.higherKinds

/** Authentication service */
class AuthenticationService[F[_]: Monad](
    config: AuthenticationConfiguration,
    authenticationRepository: AuthenticationProviderRepository[F],
    userRepository: UserRepository[F],
    tiersRepository: TierRepository[F],
    registrationAttemptRepository: RegistrationAttemptRepository[F],
    registrationAttemptIdGenerator: RegistrationAttemptIdGenerator,
    apiKeyService: ApiKeyService[F],
    clock: Clock[F]
) {

  type Result[T] = Either[Throwable, T]

  /* return login url for authentication */
  def loginUrl: String = config.loginUrl

  /* resolve auth code */
  def resolveAuthCode(code: String): F[Result[Either[RegistrationAttemptId, User]]] =
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .flatMap(getUserOrStartRegistration)
      .value

  /* confirms started registration */
  def acceptRegistration(registrationAttemptId: RegistrationAttemptId): F[Result[User]] =
    (for {
      registrationAttempt <- EitherT(registrationAttemptRepository.pop(registrationAttemptId))
      defaultTier <- EitherT.right(tiersRepository.getDefault)
      now <- EitherT.right(clock.currentInstant)
      currentUsage = defaultTier.getCurrentUsage(now)
      createUser = registrationAttempt.toCreateUser(config.provider, defaultTier.tierId)
      userId <- EitherT(userRepository.createUser(createUser))
      _ <- EitherT.right[Throwable](apiKeyService.initializeApiKeys(userId, currentUsage))
    } yield createUser.toUser(userId)).value

  private def exchangeCodeForAccessToken(code: String) =
    EitherT(authenticationRepository.exchangeCodeForAccessToken(code))

  private def fetchEmail(accessToken: String) = EitherT(authenticationRepository.fetchEmail(accessToken))

  private def getUserOrStartRegistration(email: String) = {
    val result = EitherT.right(for {
      now <- clock.currentInstant
      user <- userRepository.getUserByEmailAddress(email)
    } yield user.toRight(RegistrationAttempt(registrationAttemptIdGenerator.generateId, email, now, config.provider)))

    result.flatMap {
      case Right(user) =>
        EitherT.rightT[F, Throwable](user.asRight[String])
      case Left(registrationAttempt) =>
        EitherT(registrationAttemptRepository.save(registrationAttempt))
          .map(_ => registrationAttempt.id.asLeft[User])
    }
  }
}
