package tech.cryptonomic.nautilus.cloud.domain

import java.time.Instant

import cats.Monad
import cats.data.OptionT
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, CreateApiKey, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, _}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.resources.Resource.ResourceId
import tech.cryptonomic.nautilus.cloud.domain.resources.ResourceRepository
import tech.cryptonomic.nautilus.cloud.domain.tier.TierRepository
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{UpdateUser, User, UserRepository}

import scala.language.higherKinds
import scala.util.Random

/** User service implementation */
class UserService[F[_]](
    userRepo: UserRepository[F],
    apiKeyRepo: ApiKeyRepository[F],
    resourcesRepository: ResourceRepository[F],
    tiersRepository: TierRepository[F]
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

}
