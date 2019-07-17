package tech.cryptonomic.nautilus.cloud.adapters.akka

import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

/* Simplifies mapping over A => Route type */
object ResponseMappers {
  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  implicit class ResponseMapper[A](val response: A => Route) extends AnyVal {
    def embeddedMap[S](f: RouteResult => S): A => RequestContext => Future[S] = response.andThen(_.andThen(_.map(f)))
  }
}
