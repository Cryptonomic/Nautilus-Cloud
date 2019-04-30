package tech.cryptonomic.cloud.nautilus.services

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.cloud.nautilus.model.{ApiKey, User, UserReg}
import tech.cryptonomic.cloud.nautilus.repositories.{ApiKeyRepo, UserRepo}

import scala.language.higherKinds

class UserServiceImpl[F[_]](userRepo: UserRepo, apiKeyRepo: ApiKeyRepo, transactor: Transactor[F])(
    implicit bracket: Bracket[F, Throwable]
) extends UserService[F] {
  override def createUser(userReg: UserReg): F[Unit] =
    userRepo.createUser(userReg).run.map(_ => ()).transact(transactor)

  override def updateUser(user: User): F[Unit] =
    userRepo.updateUser(user).run.map(_ => ()).transact(transactor)

  override def getUser(userId: Int): F[Option[User]] =
    userRepo.getUser(userId).option.transact(transactor)

  override def getUserApiKeys(userId: Int): F[List[ApiKey]] =
    apiKeyRepo.getUserApiKeys(userId).to[List].transact(transactor)
}
