package tech.cryptonomic.nautilus.cloud.tools
import java.time.Instant

import cats.Applicative
import cats.effect.Clock
import cats.implicits._

import scala.concurrent.duration.{MILLISECONDS, NANOSECONDS, TimeUnit}
import scala.language.higherKinds

class FixedClock[F[_]: Applicative](time: Instant) extends Clock[F] {
  override def realTime(unit: TimeUnit): F[Long] = unit.convert(time.toEpochMilli, MILLISECONDS).pure[F]
  override def monotonic(unit: TimeUnit): F[Long] = unit.convert(time.getNano, NANOSECONDS).pure[F]
}
