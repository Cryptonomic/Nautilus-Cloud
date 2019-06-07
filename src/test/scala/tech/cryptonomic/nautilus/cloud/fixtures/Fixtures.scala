package tech.cryptonomic.nautilus.cloud.fixtures

import java.sql.Timestamp

import tech.cryptonomic.nautilus.cloud.adapters.authentication.github.GithubConfig
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.user.{User, UserWithoutId}

import scala.concurrent.duration._

trait Fixtures {
  val exampleApiKey = ApiKey(0, "", 1, 2, 3, None, None)

  val exampleUser = User(1, "someUserName", "email@example.com", "user", new Timestamp(1), None, None)

  val exampleUserWithoutId = UserWithoutId("someUserName", "email@example.com", "user", new Timestamp(1), None, None)

  val examleUsageLeft = UsageLeft("apikey", 500, 15000)

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
      |  "registrationDate": 1,
      |  "userName": "someUserName"
      |}
    """.stripMargin

  val exampleUserJson =
    """
      |{
      |  "userId": 1,
      |  "userRole": "user",
      |  "userEmail": "email@example.com",
      |  "registrationDate": 1,
      |  "userName": "someUserName"
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

  val githubConfig = GithubConfig(
    clientId = "clientId",
    clientSecret = "clientSecret",
    accessTokenUrl = "http://localhost:8089/login/oauth/access_token",
    loginUrl = "http://localhost:8089/login/oauth/authorize",
    emailsUrl = "http://localhost:8089/user/emails",
    connectionTimeout = 100 milliseconds,
    readTimeout = 100 milliseconds
  )
}
