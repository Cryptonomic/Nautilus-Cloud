package tech.cryptonomic.cloud.nautilus.security

import cats.Monad
import cats.implicits._
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.duration._
import scala.language.higherKinds
import scala.util.Try

class SttpOauthRepository[F[_]](config: AuthProviderConfig)(
    implicit monad: Monad[F],
    sttpBackend: SttpBackend[F, Nothing]
) extends OauthRepository[F] {

  private val unknownError: F[Result[String]] = monad.pure(Left(SttpOauthServiceException("Unknown error")))
  private val embeddedError: Throwable => F[Result[String]] = error =>
    monad.pure(Left(SttpOauthServiceException(cause = error)))

  override def exchangeCodeForAccessToken(code: String): F[Result[String]] = safeCall(
    sttp
      .body(
        Map(
          "client_id" -> config.clientId,
          "client_secret" -> config.clientSecret,
          "code" -> code
        )
      )
      .post(uri"${config.accessTokenUrl}")
      .readTimeout(config.readTimeout.milliseconds)
      .header("Accept", "application/json")
      .send()
      .map(
        _.body.left
          .map(SttpOauthServiceException(_))
          .flatMap(decode[TokenResponse](_).map(_.access_token))
      )
  )

  override def fetchEmail(accessToken: String): F[Result[String]] = safeCall(
    sttp
      .get(uri"${config.getEmailsUrl}")
      .readTimeout(config.readTimeout.milliseconds)
      .header("Authorization", s"Bearer $accessToken")
      .send()
      .map(
        _.body.left
          .map(SttpOauthServiceException(_))
          .flatMap(
            decode[List[EmailResponse]](_).flatMap(extractEmail)
          )
      )
  )

  private def extractEmail(emailResponse: List[EmailResponse]): Result[String] =
    emailResponse
      .find(response => response.primary && response.verified)
      .map(_.email)
      .toRight(SttpOauthServiceException("No primary and verified email available for a user"))

  private def safeCall(value: => F[Result[String]]): F[Result[String]] =
    Try(value).recover {
      case error: Throwable => embeddedError(error)
      case _ => unknownError
    }.get
}

final case class EmailResponse(email: String, primary: Boolean, verified: Boolean)

final case class TokenResponse(access_token: String)

final case class SttpOauthServiceException(message: String = "", cause: Throwable = null)
    extends Exception(message, cause)
