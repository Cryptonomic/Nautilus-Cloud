package tech.cryptonomic.cloud.nautilus.domain

import java.time.Instant

import cats.Monad
import cats.implicits._
import cats.data.EitherT
import tech.cryptonomic.cloud.nautilus.adapters.sttp.GithubConfig
import tech.cryptonomic.cloud.nautilus.domain.security.{GithubRepository, Session}
import tech.cryptonomic.cloud.nautilus.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.cloud.nautilus.domain.user.{CreateUser, Role, User, UserRepository}

import scala.language.higherKinds

class SecurityService[F[_]: Monad](
    config: GithubConfig,
    githubRepository: GithubRepository[F],
    userRepository: UserRepository[F]
) {

  type Result[T] = Either[Throwable, T]

  val scopes = List("user:email")

  def loginUrl: String = config.loginUrl + s"?scope=${scopes.mkString(",")}&client_id=${config.clientId}"

  def resolveAuthCode(code: String): F[Result[Session]] =
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .flatMap(getOrCreateUser)
      .map(_.asSession)
      .value

  private def getOrCreateUser(email: String): EitherT[F, Throwable, User] = getUserByEmailAddress(email).flatMap {
      case Some(user) => EitherT.rightT(user)
      case None => createUser(email)
    }

  private def getUserByEmailAddress(email: String): EitherT[F, Throwable, Option[User]] = {
    EitherT(userRepository.getUserByEmailAddress(email).map(Right(_)))
  }

  private def createUser(email: String): EitherT[F, Throwable, User] = {
    val userWithoutId = CreateUser(email, Role.User, Instant.now(), Github, None)
    EitherT(userRepository.createUser(userWithoutId))
      .map(userWithoutId.toUser)
  }

  private def exchangeCodeForAccessToken(code: String) = EitherT(githubRepository.exchangeCodeForAccessToken(code))

  private def fetchEmail(accessToken: String) = EitherT(githubRepository.fetchEmail(accessToken))
}
