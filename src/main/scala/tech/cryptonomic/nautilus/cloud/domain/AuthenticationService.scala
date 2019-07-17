package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.data.EitherT
import tech.cryptonomic.nautilus.cloud.domain.authentication.{
  AuthenticationConfiguration,
  AuthenticationProviderRepository,
  Session
}
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, User, UserRepository}

import scala.language.higherKinds

/** Authentication service */
class AuthenticationService[F[_]: Monad](
    config: AuthenticationConfiguration,
    authenticationRepository: AuthenticationProviderRepository[F],
    userRepository: UserRepository[F]
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
    val createUser = CreateUser(email, Role.defaultRole, Instant.now(), config.provider)
    EitherT(userRepository.createUser(createUser))
      .map(createUser.toUser)
  }
}
