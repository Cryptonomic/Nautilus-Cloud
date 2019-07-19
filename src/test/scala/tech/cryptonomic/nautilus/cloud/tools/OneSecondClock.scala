package tech.cryptonomic.nautilus.cloud.tools

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

import cats.Applicative
import cats.effect.Clock
import cats.implicits._

import scala.concurrent.duration.{MILLISECONDS, TimeUnit}
import scala.language.higherKinds

/* This is an implementation of a clock which gives you one second difference between two succeeding calls */
class OneSecondClock[F[_]: Applicative](startingTime: Instant) extends Clock[F] {
  val counter = new AtomicInteger()

  def reset(): Unit = counter.set(0)

  override def realTime(unit: TimeUnit): F[Long] =
    unit.convert(startingTime.plusSeconds(counter.getAndIncrement()).toEpochMilli, MILLISECONDS).pure[F]
  override def monotonic(unit: TimeUnit): F[Long] = ???
}
