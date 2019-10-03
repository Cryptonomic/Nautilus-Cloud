package tech.cryptonomic.nautilus.cloud.domain.pagination

final case class PaginatedResult[F](pagesTotal: Long, resultCount: Long, result: List[F])
