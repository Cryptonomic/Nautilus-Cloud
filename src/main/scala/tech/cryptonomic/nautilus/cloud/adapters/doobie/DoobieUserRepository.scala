package tech.cryptonomic.nautilus.cloud.adapters.doobie

import java.time.Instant

import cats.{Applicative, Monad}
import cats.effect.Bracket
import cats.implicits._
import doobie.enum.SqlState
import doobie.implicits._
import doobie.util.transactor.Transactor
import tech.cryptonomic.nautilus.cloud.domain.authentication.AuthenticationProviderRepository.Email
import tech.cryptonomic.nautilus.cloud.domain.pagination.{PaginatedResult, Pagination}
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.{CreateUser, UpdateUser, User, UserRepository}

import scala.language.higherKinds

/** Trait representing User repo queries */
class DoobieUserRepository[F[_]: Applicative](transactor: Transactor[F])(
    implicit bracket: Bracket[F, Throwable],
    monad: Monad[F]
) extends UserRepository[F]
    with UserQueries {

  val UNIQUE_VIOLATION = SqlState("23505")

  /** Creates user */
  override def createUser(user: CreateUser): F[Either[Throwable, UserId]] =
    createUserQuery(user)
      .withUniqueGeneratedKeys[UserId]("userid")
      .attemptSomeSqlState {
        case UNIQUE_VIOLATION => DoobieUniqueUserViolationException("UNIQUE_VIOLATION"): Throwable
      }
      .transact(transactor)

  /** Updates user */
  override def updateUser(id: UserId, user: UpdateUser, now: => Instant): F[Unit] =
    if (user.isEmpty)
      ().pure[F]
    else
      updateUserQuery(id, user, now).run.void.transact(transactor)

  /** Delete user */
  override def deleteUser(id: UserId, now: Instant): F[Unit] = deleteUserQuery(id, now).run.void.transact(transactor)

  /** Returns user */
  override def getUser(id: UserId): F[Option[User]] =
    getUserQuery(id).option.transact(transactor)

  /** Returns user by email address */
  override def getUserByEmailAddress(email: String): F[Option[User]] =
    getUserByEmailQuery(email).option.transact(transactor)

  /** Returns all users */
  override def getUsers(searchCriteria: SearchCriteria)(
      pagination: Pagination
  ): F[PaginatedResult[User]] = {

    val paginatedResult = for {
      resultCount <- getUsersCountQuery(searchCriteria).unique
      users <- getUsersQuery(searchCriteria)(pagination).to[List]
    } yield PaginatedResult(pagination.pagesTotal(resultCount.toInt), resultCount, users)

    paginatedResult.transact(transactor)
  }
}

final case class SearchCriteria(
    userId: Option[UserId] = None,
    email: Option[Email] = None,
    apiKey: Option[String] = None
) extends Product
    with Serializable

final case class DoobieUniqueUserViolationException(message: String) extends Exception(message)
