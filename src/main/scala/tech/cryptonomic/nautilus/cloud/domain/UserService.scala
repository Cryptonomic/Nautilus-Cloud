package tech.cryptonomic.nautilus.cloud.domain

import tech.cryptonomic.nautilus.cloud.domain.apiKey.{ApiKey, ApiKeyRepository}
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.{UpdateUser, User, UserRepository}

import scala.language.higherKinds

/** User service implementation */
class UserService[F[_]](userRepo: UserRepository[F], apiKeyRepo: ApiKeyRepository[F]) {

  /** Get current user */
  def getCurrentUser(session: Session): F[Option[User]] = userRepo.getUserByEmailAddress(session.email)

  /** Updated user */
  def updateUser(id: Int, user: UpdateUser): F[Unit] =
    userRepo.updateUser(id, user)

  /** Returns user with given ID */
  def getUser(userId: Int): F[Option[User]] =
    userRepo.getUser(userId)

  /** Returns API Keys for user with given ID */
  def getUserApiKeys(userId: Int): F[List[ApiKey]] =
    apiKeyRepo.getUserApiKeys(userId)
}
