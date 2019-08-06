package tech.cryptonomic.nautilus.cloud.application

import org.scalatest._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.Environment
import tech.cryptonomic.nautilus.cloud.domain.resources.{CreateResource, Resource}
import tech.cryptonomic.nautilus.cloud.fixtures.Fixtures
import tech.cryptonomic.nautilus.cloud.tools.IdContext

class ResourceApplicationTest
    extends WordSpec
    with Matchers
    with Fixtures
    with EitherValues
    with OptionValues
    with BeforeAndAfterEach
    with OneInstancePerTest {

  val context = new IdContext
  val sut = context.resourceApplication

  "ResourceApplication" should {
      "store a resource" in {
        // given
        val id = sut.createResource(
          CreateResource(
            resourceName = "Tezos Alphanet Dev",
            description = "Conseil alphanet development environment",
            platform = "tezos",
            network = "alphanet",
            environment = Environment.Development
          )
        )

        // when
        val resource = sut.getResource(id)

        // then
        id shouldBe (1)
        resource.value shouldBe (
          Resource(
            resourceId = 1,
            resourceName = "Tezos Alphanet Dev",
            description = "Conseil alphanet development environment",
            platform = "tezos",
            network = "alphanet",
            environment = Environment.Development
          )
        )
      }

      "get all resources" in {
        // given
        sut.createResource(
          CreateResource(
            resourceName = "Tezos Alphanet Dev",
            description = "Conseil alphanet development environment",
            platform = "tezos",
            network = "alphanet",
            environment = Environment.Development
          )
        )
        sut.createResource(
          CreateResource(
            resourceName = "Tezos Mainnet Conseil Prod",
            description = "Conseil mainnet production environment",
            platform = "tezos",
            network = "mainnet",
            environment = Environment.Production
          )
        )

        // expect
        sut.getResources shouldBe (List(
          Resource(
            resourceId = 1,
            resourceName = "Tezos Alphanet Dev",
            description = "Conseil alphanet development environment",
            platform = "tezos",
            network = "alphanet",
            environment = Environment.Development
          ),
          Resource(
            resourceId = 2,
            resourceName = "Tezos Mainnet Conseil Prod",
            description = "Conseil mainnet production environment",
            platform = "tezos",
            network = "mainnet",
            environment = Environment.Production
          )
        ))
      }
    }
}
