package tech.cryptonomic.nautilus.cloud.fixtures

import java.time.{Instant, ZonedDateTime}

import cats.implicits._
import com.github.tomakehurst.wiremock.client.WireMock._
import tech.cryptonomic.nautilus.cloud.domain.apiKey._
import tech.cryptonomic.nautilus.cloud.domain.authentication.{RegistrationConfirmation, Session}
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource}
import tech.cryptonomic.nautilus.cloud.domain.tier._
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user._

import scala.language.higherKinds

trait Fixtures {
  val time = ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant

  val time5m = ZonedDateTime.parse("2019-05-27T00:05:00.000+01:00").toInstant

  val time5m2 = ZonedDateTime.parse("2019-05-27T00:10:00.000+01:00").toInstant

  val time24h = ZonedDateTime.parse("2019-05-27T00:00:00.000+01:00").toInstant

  val time24h2 = ZonedDateTime.parse("2019-05-28T00:00:00.000+01:00").toInstant

  val exampleCreateApiKey =
    CreateApiKey("cce27d90-2d8a-403f-a5b8-84f771e38629", Environment.Development, 2, time, None)
  val exampleApiKey = ApiKey(1, "cce27d90-2d8a-403f-a5b8-84f771e38629", Environment.Development, 1, Some(time), None)

  val exampleUser = User(1, "email@example.com", Role.User, time, Github, true, false, None, None)

  val exampleCreateUser = CreateUser("email@example.com", Role.User, time, Github, 1, true, true)

  val exampleUpdateUser = UpdateUser()

  val exampleConfirmRegistration = RegistrationConfirmation("some-id")

  val userSession = Session(1, "email@example.com", AuthenticationProvider.Github, Role.User, false)
  val adminSession = Session(1, "email@example.com", AuthenticationProvider.Github, Role.Administrator, true)

  val exampleCreateTier = CreateTier("some description", Usage(1, 2), 3)
  val exampleUpdateTier = UpdateTier("some description", Usage(1, 2), 3, Instant.now.some)

  val exampleUsageLeft = UsageLeft("apikey", Usage(500, 15000))

  val exampleResource = Resource(0, "dev", "Development", "tezos", "alphanet", Environment.Development)

  val exampleCreateResource = CreateResource("dev", "Development", "tezos", "alphanet", Environment.Development)

  val exampleCreateApiKeyRequestJson = """{"resourceId": 1, "tierId": 2}"""

  val exampleApiKeyAsJson =
    """
      |  [{
      |    "resourceId": 1,
      |    "keyId": 0,
      |    "key": "",
      |    "userId": 2
      |  }]
    """.stripMargin

  val exampleUserRegJson =
    """
      |{
      |  "userRole": "user",
      |  "userEmail": "email@example.com",
      |  "registrationDate": "2019-05-27T18:03:48.081+01:00",
      |  "accountSource": "github"
      |}
    """.stripMargin

  val exampleUserJson =
    """
      |{
      |  "userId": 1,
      |  "userRole": "user",
      |  "userEmail": "email@example.com",
      |  "registrationDate": "2019-05-27T17:03:48.081Z",
      |  "accountSource": "github"
      |}
    """.stripMargin

  val exampleUsageJson =
    """
        |[{
        |  "key":"apikey",
        |  "daily":500,
        |  "monthly":15000
        |}]
      """.stripMargin

  val exampleApiKeyStats5m = List(ApiKeyStats(time5m, 1, Some("apikey")), ApiKeyStats(time5m2, 1, None))

  val exampleApiKeyStats24h = List(ApiKeyStats(time24h, 1, Some("apikey")), ApiKeyStats(time24h2, 1, None))

  val exampleRouteStats5m = List(RouteStats(time5m, 1, "url", Some("apikey")), RouteStats(time5m2, 1, "url", None))

  val exampleRouteStats24h = List(RouteStats(time24h, 1, "url", Some("apikey")), RouteStats(time24h2, 1, "url", None))

  val exampleIpStats5m = List(IpStats(time5m, 1, "ip", Some("apikey")), IpStats(time5m2, 1, "ip", None))

  val exampleIpStats24h = List(IpStats(time24h, 1, "ip", Some("apikey")), IpStats(time24h, 1, "ip", None))

  def stubAuthServiceFor(authCode: String, email: String): Unit = {
    val accessToken = """stubbed-access-token"""

    stubFor(
      post(urlEqualTo("/login/oauth/access_token"))
        .withRequestBody(equalTo(s"client_id=client-id&client_secret=client-secret&code=$authCode"))
        .willReturn(
          aResponse()
            .withBody("""{"access_token": """" + accessToken + """"}""")
        )
    )

    stubFor(
      get(urlEqualTo("/user/emails"))
        .withHeader("Authorization", equalTo("Bearer " + accessToken))
        .willReturn(
          aResponse()
            .withBody(s"""[
                         |    {
                         |        "email": "$email",
                         |        "primary": true,
                         |        "verified": true,
                         |        "visibility": "public"
                         |    }
                         |]""".stripMargin)
        )
    )
  }
}
