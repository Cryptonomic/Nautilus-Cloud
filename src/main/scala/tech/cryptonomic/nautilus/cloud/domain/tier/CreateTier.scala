package tech.cryptonomic.nautilus.cloud.domain.tier

case class CreateTier(
    description: String,
    monthlyHits: Int,
    dailyHits: Int,
    maxResultSetSize: Int
) {
  def toTier(name: TierName): Tier =
    Tier(name, List(TierConfiguration(description, monthlyHits, dailyHits, maxResultSetSize)))
}
