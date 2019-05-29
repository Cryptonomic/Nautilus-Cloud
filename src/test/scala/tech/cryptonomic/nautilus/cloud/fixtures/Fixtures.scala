package tech.cryptonomic.nautilus.cloud.fixtures

import java.sql.Timestamp

import tech.cryptonomic.nautilus.cloud.domain.apiKey.ApiKey
import tech.cryptonomic.nautilus.cloud.domain.user.{User, UserWithoutId}

trait Fixtures {
  val exampleApiKey = ApiKey(0, "", 1, 2, 3, None, None)

  val exampleUser = User(1, "someUserName", "email@example.com", "user", new Timestamp(1), None, None)

  val exampleUserWithoutId = UserWithoutId("someUserName", "email@example.com", "user", new Timestamp(1), None, None)

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

}
