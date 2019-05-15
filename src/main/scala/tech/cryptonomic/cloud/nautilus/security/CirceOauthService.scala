package tech.cryptonomic.cloud.nautilus.security

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser._

import scala.language.higherKinds

class CirceOauthService[F[_]: Monad](config: AuthProviderConfig)(implicit sttpBackend: SttpBackend[F, Nothing]) extends OauthService[F] {

  type Result[T] = Either[Throwable, T]

  override def resolveAuthCode(code: String): F[Result[String]] = exchangeCodeForAccessToken(code)
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
        .post(uri"${config.loginUrl}")
        .header("Accept", "application/json")
        .send()
        .map(
          _.body.left
            .map(new RuntimeException(_))
            .flatMap(decode[TokenResponse](_).map(_.access_token))
        )
    )

  private def fetchEmail(accessToken: String): EitherT[F, Throwable, String] =
    EitherT(
      sttp
        .get(uri"${config.getUserUrl}")
        .header("Authorization", s"Bearer $accessToken")
        .send()
        .map(
          _.body.left
            .map(new RuntimeException(_))
            .flatMap(decode[UserResponse](_).map(_.email))
        )
    )
}

case class UserResponse(email: String)

case class TokenResponse(access_token: String)
