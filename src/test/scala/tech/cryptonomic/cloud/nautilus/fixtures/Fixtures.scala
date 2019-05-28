package tech.cryptonomic.cloud.nautilus.fixtures

import java.time.{Instant, ZonedDateTime}

import tech.cryptonomic.cloud.nautilus.domain.apiKey.ApiKey
import tech.cryptonomic.cloud.nautilus.domain.user.AuthenticationProvider.Github
import tech.cryptonomic.cloud.nautilus.domain.user.{CreateUser, Role, UpdateUser, User}

trait Fixtures {
  val time = ZonedDateTime.parse("2019-05-27T18:03:48.081+01:00").toInstant

  val exampleApiKey = ApiKey(0, "", 1, 2, 3, None, None)

  val exampleUser = User(1, "email@example.com", Role.User, time, Github, None)

  val exampleCreateUser = CreateUser("email@example.com", Role.User, time, Github, None)

  val exampleUpdateUser = UpdateUser("email@example.com", Role.User, Github, None)

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
}
