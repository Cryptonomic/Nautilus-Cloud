package tech.cryptonomic.nautilus.cloud.adapters.metering.api
import cats.Applicative
import com.softwaremill.sttp.SttpBackend
import tech.cryptonomic.nautilus.cloud.adapters.metering.MeteringApiConfig
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyStats, IpStats, RouteStats}
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApiRepository

import scala.language.higherKinds
import cats.implicits._
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser._
import tech.cryptonomic.nautilus.cloud.domain.metering.api.MeteringApiRepository._

/** Implementation of API for Metering stats */
class SttpMeteringApiRepository[F[_]: Applicative](config: MeteringApiConfig)(
    implicit sttpBackend: SttpBackend[F, Nothing]
) extends MeteringApiRepository[F] {
  /** Fetches ApiKey stats per 5m */
  override def getApiKey5mStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]] = {
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

  /** Fetches ApiKey stats per 24h */
  override def getApiKey24hStats(apiKeys: List[ApiKey]): F[Result[List[ApiKeyStats]]] = {
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

  /** Fetches Route stats per 5m */
  override def getRoute5mStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]] = {
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

  /** Fetches Route stats per 24h */
  override def getRoute24hStats(apiKeys: List[ApiKey]): F[Result[List[RouteStats]]] = {
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

  /** Fetches IP stats per 5m */
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

  /** Fetches IP stats per 24h */
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

  /** Custom exception class */
  final case class QueryException(message: String = "", cause: Throwable = null) extends Exception(message, cause)
}
