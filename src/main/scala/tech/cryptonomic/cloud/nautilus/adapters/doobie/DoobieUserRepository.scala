package tech.cryptonomic.cloud.nautilus.adapters.doobie

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.cloud.nautilus.domain.user.{User, UserRepository, UserWithoutId}

import scala.language.higherKinds

/** Trait representing User repo queries */
class DoobieUserRepository[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable])
    extends UserRepository[F]
    with UserQueries {

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
