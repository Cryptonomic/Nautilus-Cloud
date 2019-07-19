package tech.cryptonomic.nautilus.cloud.fixtures

import java.time.{Instant, ZonedDateTime}

import com.github.tomakehurst.wiremock.client.WireMock._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, CreateApiKey, CreateApiKeyRequest, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.tier.{CreateTier, UpdateTier}
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider
import tech.cryptonomic.nautilus.cloud.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, Role, UpdateUser, User}

trait Fixtures {
  val time = ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant

  val exampleCreateApiKey = CreateApiKey("", 1, 2, 3, None, None)
  val exampleApiKey = ApiKey(1, "", 1, 2, 3, None, None)

  val exampleUser = User(1, "email@example.com", Role.User, time, Github, None)

  val exampleCreateUser = CreateUser("email@example.com", Role.User, time, Github, None)

  val exampleUpdateUser = UpdateUser(Role.User, None)

  val userSession = Session("user@domain.com", AuthenticationProvider.Github, Role.User)
  val adminSession = Session("user@domain.com", AuthenticationProvider.Github, Role.Administrator)

  val exampleCreateTier = CreateTier("some description", 1, 2, 3)
  val exampleUpdateTier = UpdateTier("some description", 1, 2, 3, Some(Instant.now))

  val exampleUsageLeft = UsageLeft("apikey", 500, 15000)

  val exampleResource = Resource(0, "dev", "Development", "tezos", "alphanet")

  val exampleCreateResource = CreateResource("dev", "Development", "tezos", "alphanet")

  val exampleCreateApiKeyRequest = CreateApiKeyRequest(1, 2)

  val exampleCreateApiKeyRequestJson = """{"resourceId": 1, "tierId": 2}"""

  val exampleApiKeyAsJson =
    """
      |  [{
      |    "resourceId": 1,
      |    "tierId": 3,
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
