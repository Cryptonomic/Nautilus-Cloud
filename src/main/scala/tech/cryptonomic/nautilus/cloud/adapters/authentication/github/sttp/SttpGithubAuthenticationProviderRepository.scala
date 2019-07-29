package tech.cryptonomic.nautilus.cloud.adapters.authentication.github.sttp

import cats.Applicative
import cats.implicits._
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser._
import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.GithubConfig
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.AuthenticationProviderRepository
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.AuthenticationProviderRepository._

import scala.language.higherKinds
import scala.util.Try

/* Github authentication provider */
class SttpGithubAuthenticationProviderRepository[F[_]: Applicative](config: GithubConfig)(
    implicit sttpBackend: SttpBackend[F, Nothing]
) extends AuthenticationProviderRepository[F] {

  private val unknownError: Result[String] = Left(SttpGithubAuthenticationProviderException("Unknown error"))
  private val embeddedError: Throwable => Result[String] = error =>
    Left(SttpGithubAuthenticationProviderException(cause = error))

  /* exchange code for an access token */
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

  /* fetch an email using an access token */
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
      case error: Throwable => embeddedError(error).pure[F]
      case _ => unknownError.pure[F]
    }.get

  final private case class EmailResponse(email: String, primary: Boolean, verified: Boolean)

  final private case class TokenResponse(access_token: String)
}

/* Exception for errors realated to Sttp */
final case class SttpGithubAuthenticationProviderException(message: String = "", cause: Throwable = null)
    extends Exception(message, cause)
