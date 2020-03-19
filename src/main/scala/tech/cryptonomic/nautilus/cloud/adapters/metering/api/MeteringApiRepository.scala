package tech.cryptonomic.nautilus.cloud.adapters.metering.api
import cats.Applicative
import com.softwaremill.sttp.SttpBackend
import tech.cryptonomic.nautilus.cloud.adapters.metering.MeteringApiConfig
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyStats, IpStats, RouteStats}
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApi

import scala.language.higherKinds
import cats.implicits._
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser._
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApi._

class MeteringApiRepository[F[_]: Applicative](config: MeteringApiConfig)(
    implicit sttpBackend: SttpBackend[F, Nothing]
) extends MeteringApi[F] {
  override def getApiKeyQueries5mStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]] = {
    apiKeys.map("apiKey" -> _.key)
    sttp
      .get(uri"${config.host}:${config.port}/queries/5m".params(apiKeys.map("apiKey" -> _.key): _*))
      .readTimeout(config.readTimeout)
      .send()
      .map {
        _.body.left
          .map(QueryException(_))
          .flatMap(
            decode[List[ApiKeyStats]](_)
          )
      }
  }

  override def getApiKeyQueries24hStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]] = {
    apiKeys.map("apiKey" -> _.key)
    sttp
      .get(uri"${config.host}:${config.port}/queries/24h".params(apiKeys.map("apiKey" -> _.key): _*))
      .readTimeout(config.readTimeout)
      .send()
      .map {
        _.body.left
          .map(QueryException(_))
          .flatMap(
            decode[List[ApiKeyStats]](_)
          )
      }
  }

  override def getRoute5mStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]] = {
    apiKeys.map("apiKey" -> _.key)
    sttp
      .get(uri"${config.host}:${config.port}/routes/5m".params(apiKeys.map("apiKey" -> _.key): _*))
      .readTimeout(config.readTimeout)
      .send()
      .map {
        _.body.left
          .map(QueryException(_))
          .flatMap(
            decode[List[RouteStats]](_)
          )
      }
  }
  override def getRoute24hStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]] = {
    apiKeys.map("apiKey" -> _.key)
    sttp
      .get(uri"${config.host}:${config.port}/routes/24h".params(apiKeys.map("apiKey" -> _.key): _*))
      .readTimeout(config.readTimeout)
      .send()
      .map {
        _.body.left
          .map(QueryException(_))
          .flatMap(
            decode[List[RouteStats]](_)
          )
      }
  }
  override def getIp5mStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]] = {
    apiKeys.map("apiKey" -> _.key)
    sttp
      .get(uri"${config.host}:${config.port}/queries/5m".params(apiKeys.map("apiKey" -> _.key): _*))
      .readTimeout(config.readTimeout)
      .send()
      .map {
        _.body.left
          .map(QueryException(_))
          .flatMap(
            decode[List[IpStats]](_)
          )
      }
  }
  override def getIp24hStats(apiKeys: List[ApiKey]): F[Result[List[IpStats]]] = {
    apiKeys.map("apiKey" -> _.key)
    sttp
      .get(uri"${config.host}:${config.port}/queries/5m".params(apiKeys.map("apiKey" -> _.key): _*))
      .readTimeout(config.readTimeout)
      .send()
      .map {
        _.body.left
          .map(QueryException(_))
          .flatMap(
            decode[List[IpStats]](_)
          )
      }
  }

  final case class QueryException(message: String = "", cause: Throwable = null) extends Exception(message, cause)

}
