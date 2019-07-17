package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.data.OptionT
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, CreateApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.Permission
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.ResourceRepository
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User, UserRepository}

import scala.language.higherKinds
import scala.util.Random
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService._


/** User service implementation */
class UserService[F[_]](
    userRepo: UserRepository[F],
    apiKeyRepo: ApiKeyRepository[F],
    resourcesRepository: ResourceRepository[F]
)(implicit monad: Monad[F]) {

  /** Get current user */
  def getCurrentUser(implicit session: Session): F[Option[User]] = userRepo.getUserByEmailAddress(session.email)

  /** Updated user */
  def updateUser(id: Int, user: UpdateUser)(implicit session: Session): F[Permission[Unit]] =
    requiredRole(Administrator) {
      userRepo.updateUser(id, user)
    }

  /** Returns user with given ID */
  def getUser(userId: Int)(implicit session: Session): F[Permission[Option[User]]] = requiredRole(Administrator) {
    userRepo.getUser(userId)
  }

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: Int): F[List[ApiKey]] =
    apiKeyRepo.getUserApiKeys(userId)

  /** Returns API Keys usage for user with given ID */
  def getUserApiKeysUsage(userId: Int): F[List[UsageLeft]] =
    apiKeyRepo.getKeysUsageForUser(userId)

  /** Creates API key for given userId, resourceId and tierId */
  def createApiKey(userId: UserId, resourceId: ResourceId, tierId: Int): F[Option[String]] = {
    val generatedKey = Random.alphanumeric.take(32).mkString

    // I need to add tiers here
    {
      for {
        _ <- OptionT(userRepo.getUser(userId))
        _ <- OptionT(resourcesRepository.getResource(resourceId))
      } yield
        apiKeyRepo
          .putApiKeyForUser(CreateApiKey(generatedKey, resourceId, userId, tierId, Some(Instant.now()), None))
          .map(_ => generatedKey)
    }.value.flatMap(_.sequence)
  }

}
