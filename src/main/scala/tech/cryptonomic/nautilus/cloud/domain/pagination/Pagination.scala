package tech.cryptonomic.nautilus.cloud.domain.pagination

/**
  *
  * @param page     page number (starting from 1)
  * @param pageSize items per page
  */
final case class Pagination(page: Int, pageSize: Int) {
  lazy val offset: Int = (page - 1) * pageSize
  def pagesTotal(resultCount: Int): Int = (resultCount.toFloat / pageSize).ceil.toInt
}

object Pagination {
  val allResults = Pagination(page = 1, pageSize = Int.MaxValue)

  def apply(pageSize: Option[Int], page: Option[Int]): Pagination =
    Pagination(page.getOrElse(allResults.page), pageSize.getOrElse(allResults.pageSize))
}
