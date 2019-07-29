package tech.cryptonomic.nautilus.cloud.domain.tools

import java.time.Instant

import cats.Monad
import cats.effect.Clock
import cats.implicits._

import scala.concurrent.duration.MILLISECONDS
import scala.language.higherKinds

object ClockTool {
  implicit class ExtendedClock[F[_]: Monad](val clock: Clock[F]) {
    def currentInstant: F[Instant] = clock.realTime(MILLISECONDS).map(Instant.ofEpochMilli)
  }
}
