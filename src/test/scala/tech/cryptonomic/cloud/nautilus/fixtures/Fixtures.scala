package tech.cryptonomic.cloud.nautilus.fixtures

import java.sql.Timestamp

import tech.cryptonomic.cloud.nautilus.domain.user.{User, UserWithoutId}
import tech.cryptonomic.cloud.nautilus.domain.apiKey.ApiKey

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
