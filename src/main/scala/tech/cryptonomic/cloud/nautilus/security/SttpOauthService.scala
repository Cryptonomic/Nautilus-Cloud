package tech.cryptonomic.cloud.nautilus.security

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.duration._
import scala.language.higherKinds
import scala.util.Try

class SttpOauthService[F[_]](config: AuthProviderConfig)(implicit monad: Monad[F], sttpBackend: SttpBackend[F, Nothing])
    extends OauthService[F] {

  type Result[T] = Either[Throwable, T]

  private val unknownError: F[Result[String]] = monad.pure(Left(SttpOauthServiceException("Unknown error")))
  private val embeddedError: Throwable => F[Result[String]] = error =>
    monad.pure(Left(SttpOauthServiceException(cause = error)))

  override def loginUrl: String = uri"${config.loginUrl}".queryFragment(KeyValue("client_id", config.clientId)).toString

  override def resolveAuthCode(code: String): F[Result[String]] =
    Try(resolveAuthCodeInternal(code)).recover {
      case error: Throwable => embeddedError(error)
      case _ => unknownError
    }.get

  private def resolveAuthCodeInternal(code: String): F[Result[String]] =
    exchangeCodeForAccessToken(code)
      .flatMap(fetchEmail)
      .value

  private def exchangeCodeForAccessToken(code: String): EitherT[F, Throwable, String] =
    EitherT(
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

  private def fetchEmail(accessToken: String): EitherT[F, Throwable, String] =
    EitherT(
      sttp
        .get(uri"${config.getUserUrl}")
        .readTimeout(config.readTimeout.milliseconds)
        .header("Authorization", s"Bearer $accessToken")
        .send()
        .map(
          _.body.left
            .map(SttpOauthServiceException(_))
            .flatMap(decode[UserResponse](_).map(_.email))
        )
    )
}

final case class UserResponse(email: String)

final case class TokenResponse(access_token: String)

final case class SttpOauthServiceException(message: String = "", cause: Throwable = null)
    extends Exception(message, cause)
