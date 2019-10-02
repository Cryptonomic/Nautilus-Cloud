package tech.cryptonomic.nautilus.cloud.domain.pagination

final case class PaginatedResult[F](pagesTotal: Int, resultCount: Int, result: List[F])
