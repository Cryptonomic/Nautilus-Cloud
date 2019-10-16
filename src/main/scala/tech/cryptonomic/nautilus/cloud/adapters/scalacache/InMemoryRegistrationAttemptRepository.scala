package tech.cryptonomic.nautilus.cloud.adapters.scalacache

import cats.Applicative
import cats.implicits._
import com.google.common.cache.CacheBuilder
import scalacache.guava.GuavaCache
import scalacache.modes.try_._
import scalacache.{Cache, Entry}
import tech.cryptonomic.nautilus.cloud.domain.authentication.RegistrationAttempt.RegistrationAttemptId
import tech.cryptonomic.nautilus.cloud.domain.authentication.{
  RegistrationAttempt,
  RegistrationAttemptNotFoundException,
  RegistrationAttemptRepository
}

import scala.concurrent.duration.{Duration, _}
import scala.language.{higherKinds, postfixOps}

class InMemoryRegistrationAttemptRepository[F[_]: Applicative](duration: Duration = 10 minutes)
    extends RegistrationAttemptRepository[F] {

  private val underlyingGuavaCache =
    CacheBuilder.newBuilder().build[String, Entry[RegistrationAttempt]]
  implicit private val cache: Cache[RegistrationAttempt] = GuavaCache(underlyingGuavaCache)

  override def save(registrationAttempt: RegistrationAttempt): F[Either[Throwable, Unit]] =
    cache
      .cachingForMemoize(registrationAttempt.id)(Some(duration))(registrationAttempt)
      .pure[F]
      .map(_.toEither.map(_ => ()))

  override def pop(id: RegistrationAttemptId): F[Either[Throwable, RegistrationAttempt]] = this.synchronized {
    val result = cache.get(id).toEither.flatMap(_.toRight(RegistrationAttemptNotFoundException(id)))

    if (result.isRight) {
      cache.remove(id)
    }

    result.pure[F]
  }
}
