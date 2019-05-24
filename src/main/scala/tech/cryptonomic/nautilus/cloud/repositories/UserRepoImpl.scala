package tech.cryptonomic.nautilus.cloud.repositories

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.repositories.dao.UserDao
import tech.cryptonomic.nautilus.cloud.model.{User, UserWithoutId}

import scala.language.higherKinds

/** Trait representing User repo queries */
class UserRepoImpl[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable])
    extends UserRepo[F]
    with UserDao {

  /** Creates user */
  override def createUser(userReg: UserWithoutId): F[Int] =
    createUserQuery(userReg).withUniqueGeneratedKeys[Int]("userid").transact(transactor)

  /** Updates user */
  override def updateUser(user: User): F[Unit] =
    updateUserQuery(user).run.map(_ => ()).transact(transactor)

  /** Returns user */
  override def getUser(userId: Int): F[Option[User]] =
    getUserQuery(userId).option.transact(transactor)
}
