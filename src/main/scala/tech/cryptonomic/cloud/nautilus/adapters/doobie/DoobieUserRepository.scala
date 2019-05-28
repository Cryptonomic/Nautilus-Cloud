package tech.cryptonomic.cloud.nautilus.adapters.doobie

import cats.Monad
import cats.effect.Bracket
import doobie.enum.SqlState
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.cloud.nautilus.domain.user.{CreateUser, UpdateUser, User, UserRepository}

import scala.language.higherKinds

/** Trait representing User repo queries */
class DoobieUserRepository[F[_]](transactor: Transactor[F])(implicit bracket: Bracket[F, Throwable], monad: Monad[F])
    extends UserRepository[F]
    with UserQueries {

  val UNIQUE_VIOLATION = SqlState("23505")

  /** Creates user */
  override def createUser(user: CreateUser): F[Either[Throwable, Int]] =
    createUserQuery(user)
      .withUniqueGeneratedKeys[Int]("userid")
      .attemptSomeSqlState {
        case UNIQUE_VIOLATION => DoobieUniqueViolationException("UNIQUE_VIOLATION"): Throwable
      }
      .transact(transactor)

  /** Updates user */
  override def updateUser(id: Int, user: UpdateUser): F[Unit] =
    updateUserQuery(id, user).run.map(_ => ()).transact(transactor)

  /** Returns user */
  override def getUser(id: Int): F[Option[User]] =
    getUserQuery(id).option.transact(transactor)

  /** Returns user by email address */
  override def getUserByEmailAddress(email: String): F[Option[User]] =
    getUserByEmailQuery(email).option.transact(transactor)
}

final case class DoobieUniqueViolationException(message: String) extends Exception(message)
