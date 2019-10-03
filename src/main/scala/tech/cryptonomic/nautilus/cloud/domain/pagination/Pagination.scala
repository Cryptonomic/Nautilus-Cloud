package tech.cryptonomic.nautilus.cloud.domain.pagination

final case class Pagination(limit: Int, page: Int) {
  lazy val offset: Int = (page - 1) * limit
  def pagesTotal(resultCount: Int): Int = (resultCount.toFloat / limit).ceil.toInt
}

object Pagination {
  val allResults = Pagination(limit = Int.MaxValue, page = 1)

  def apply(limit: Option[Int], page: Option[Int]): Pagination = Pagination(limit.getOrElse(Int.MaxValue), page.getOrElse(1))
}
