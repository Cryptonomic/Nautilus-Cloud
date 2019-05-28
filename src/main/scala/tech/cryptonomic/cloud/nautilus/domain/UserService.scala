package tech.cryptonomic.cloud.nautilus.domain

import tech.cryptonomic.cloud.nautilus.domain.apiKey.{ApiKey, ApiKeyRepository}
import tech.cryptonomic.cloud.nautilus.domain.user.{CreateUser, UpdateUser, User, UserRepository}

import scala.language.higherKinds

/** User service implementation */
class UserService[F[_]](userRepo: UserRepository[F], apiKeyRepo: ApiKeyRepository[F]) {

  /** Creates user */
  def createUser(createUser: CreateUser): F[Either[Throwable, Int]] =
    userRepo.createUser(createUser)

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
