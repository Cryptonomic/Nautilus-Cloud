package tech.cryptonomic.nautilus.cloud.domain.pagination

final case class Pagination(limit: Int, page: Int)

object Pagination {
  val allResults = Pagination(limit = Int.MaxValue, page = 1)
}