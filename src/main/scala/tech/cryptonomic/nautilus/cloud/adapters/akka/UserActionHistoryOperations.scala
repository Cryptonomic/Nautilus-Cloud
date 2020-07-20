package tech.cryptonomic.nautilus.cloud.adapters.akka

import java.time.Instant

import akka.http.scaladsl.model.{HttpMethods, RemoteAddress}
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.directives.BasicDirectives
import cats.effect.IO
import tech.cryptonomic.nautilus.cloud.domain.authentication.Session
import tech.cryptonomic.nautilus.cloud.domain.user.User.UserId
import tech.cryptonomic.nautilus.cloud.domain.user.history.{UserAction, UserHistoryService}

import scala.util.Try

/** Class with methods for gathering user history */
class UserActionHistoryOperations(userHistoryService: UserHistoryService[IO]) {

  /** Directive for logging user actions to thee DB for non-OPTIONS/GET requests */
  def logRequestWithSession(ip: RemoteAddress)(implicit session: Session): Directive[Unit] =
    BasicDirectives.extractRequest.flatMap { request =>
      val path = request.uri.path.toString()
      val user = if (path.contains("/users/me")) {
        Some(session.userId)
      } else
        Try {
          path.stripPrefix("/users/").takeWhile(_ != '/').toInt
        }.toOption
      val address = ip.toIP.map(_.ip.getHostAddress)
      val userAction = user.map(u => UserAction(u, Some(session.userId), Instant.now(), address, path))
      request.method match {
        case HttpMethods.OPTIONS | HttpMethods.GET => ()
        case _ => userAction.foreach(ua => userHistoryService.insertUserAction(ua).unsafeRunSync())
      }
      BasicDirectives.mapResponse(identity)
    }

  /** Generic log for user action */
  def logReqest(userId: UserId, ip: Option[String], action: String): Unit = {
    val userAction = UserAction(userId, Some(userId), Instant.now(), ip, action)
    userHistoryService.insertUserAction(userAction).unsafeRunSync()
  }

}
