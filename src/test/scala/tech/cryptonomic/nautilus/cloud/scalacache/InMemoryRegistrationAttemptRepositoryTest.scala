package tech.cryptonomic.nautilus.cloud.scalacache

import java.time.Instant

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.adapters.scalacache.InMemoryRegistrationAttemptRepository
import tech.cryptonomic.nautilus.cloud.domain.authentication
import tech.cryptonomic.nautilus.cloud.domain.authentication.{
  RegistrationAttempt,
  RegistrationAttemptConfiguration,
  RegistrationAttemptNotFoundException
}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider

import scala.concurrent.duration._
import scala.language.postfixOps

class InMemoryRegistrationAttemptRepositoryTest extends WordSpec with Matchers with EitherValues {

  val sut = new InMemoryRegistrationAttemptRepository(RegistrationAttemptConfiguration(50 milliseconds))

  "InMemoryRegistrationAttemptRepository" should {
      "save registration attempt" in {
        // given
        val registrationAttempt =
          RegistrationAttempt("1", "login@domain.com", Instant.now, AuthenticationProvider.Github)

        // when
        sut.save(registrationAttempt)

        // then
        sut.pop("1").right.value shouldEqual registrationAttempt
      }

      "return RegistrationAttemptNotFoundException when attempt was not found" in {
        // expect
        sut.pop("1").left.value shouldEqual RegistrationAttemptNotFoundException("1")
      }

      "return RegistrationAttemptNotFoundException when attempt has been invalidated" in {
        // given
        sut.save(
          authentication.RegistrationAttempt("1", "login@domain.com", Instant.now, AuthenticationProvider.Github)
        )

        // when
        Thread.sleep(70) // TTL for cache is set to 50 milliseconds

        // then
        sut.pop("1").left.value shouldEqual RegistrationAttemptNotFoundException("1")
      }

      "registration attempt should be available only once" in {
        // given
        sut.save(
          authentication.RegistrationAttempt("1", "login@domain.com", Instant.now, AuthenticationProvider.Github)
        )

        // when
        sut.pop("1").right.value

        // then
        sut.pop("1").left.value shouldEqual RegistrationAttemptNotFoundException("1")
      }
    }
}
