package tech.cryptonomic.nautilus.cloud.adapters.authentication.github.sttp

import cats.Monad
import cats.implicits._
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser._
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.GithubConfig
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository

import scala.language.higherKinds
import scala.util.Try

class SttpGithubAuthenticationProviderRepository[F[_]](config: GithubConfig)(
    implicit monad: Monad[F],
    sttpBackend: SttpBackend[F, Nothing]
) extends AuthenticationProviderRepository[F] {

  private val unknownError: F[Result[String]] = monad.pure(Left(SttpGithubAuthenticationProviderException("Unknown error")))
  private val embeddedError: Throwable => F[Result[String]] = error =>
    monad.pure(Left(SttpGithubAuthenticationProviderException(cause = error)))

  override def exchangeCodeForAccessToken(code: Code): F[Result[AccessToken]] = safeCall(
    sttp
      .body(
        Map(
          "client_id" -> config.clientId,
          "client_secret" -> config.clientSecret,
          "code" -> code
        )
      )
      .post(uri"${config.accessTokenUrl}")
      .readTimeout(config.readTimeout)
      .header("Accept", "application/json")
      .send()
      .map(
        _.body.left
          .map(SttpGithubAuthenticationProviderException(_))
          .flatMap(decode[TokenResponse](_).map(_.access_token))
      )
  )

  override def fetchEmail(accessToken: AccessToken): F[Result[Email]] = safeCall(
    sttp
      .get(uri"${config.emailsUrl}")
      .readTimeout(config.readTimeout)
      .header("Authorization", s"Bearer $accessToken")
      .send()
      .map(
        _.body.left
          .map(SttpGithubAuthenticationProviderException(_))
          .flatMap(
            decode[List[EmailResponse]](_).flatMap(extractEmail)
          )
      )
  )

  private def extractEmail(emailResponse: List[EmailResponse]): Result[Email] =
    emailResponse
      .find(response => response.primary && response.verified)
      .map(_.email)
      .toRight(SttpGithubAuthenticationProviderException("No primary and verified email available for a user"))

  private def safeCall(value: => F[Result[String]]): F[Result[String]] =
    Try(value).recover {
      case error: Throwable => embeddedError(error)
      case _ => unknownError
    }.get
}

final case class EmailResponse(email: String, primary: Boolean, verified: Boolean)

final case class TokenResponse(access_token: String)

final case class SttpGithubAuthenticationProviderException(message: String = "", cause: Throwable = null)
    extends Exception(message, cause)
