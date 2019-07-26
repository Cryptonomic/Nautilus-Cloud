package tech.cryptonomic.nautilus.cloud.domain

import cats.Monad
import cats.implicits._
import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository, UsageLeft}
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthorizationService.{Permission, _}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.Role.Administrator
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{UpdateUser, User, UserRepository}

import scala.language.higherKinds

/** User service implementation */
class UserService[F[_]: Monad](
    userRepo: UserRepository[F],
    apiKeyRepo: ApiKeyRepository[F]
) {

  /** Get current user */
  def getCurrentUser(implicit session: Session): F[Option[User]] = userRepo.getUserByEmailAddress(session.email)

  /** Updated user */
  def updateUser(id: UserId, user: UpdateUser)(implicit session: Session): F[Permission[Unit]] =
    requiredRole(Administrator) {
      userRepo.updateUser(id, user)
    }

  /** Returns user with given ID */
  def getUser(userId: UserId)(implicit session: Session): F[Permission[Option[User]]] = requiredRole(Administrator) {
    userRepo.getUser(userId)
  }

  /** Returns API Keys for current user with given ID */
  def getCurrentUserApiKeys(implicit session: Session): F[List[ApiKey]] =
    apiKeyRepo.getUserApiKeys(session.id)

  /** Returns API Keys usage for current user with given ID */
  def getCurrentUserApiKeysUsage(implicit session: Session): F[List[UsageLeft]] =
    apiKeyRepo.getKeysUsageForUser(session.id)

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: UserId)(implicit session: Session): F[Permission[List[ApiKey]]] =
    requiredRole(Administrator) {
      apiKeyRepo.getUserApiKeys(userId)
    }

  /** Returns API Keys usage for user with given ID */
  def getUserApiKeysUsage(userId: UserId)(implicit session: Session): F[Permission[List[UsageLeft]]] =
    requiredRole(Administrator) {
      apiKeyRepo.getKeysUsageForUser(userId)
    }
}
