package tech.cryptonomic.nautilus.cloud.adapters.endpoints.schemas

import java.time.{Instant, ZonedDateTime}

import endpoints.generic

/** Schema used for Instant endpoints */
trait InstantSchema extends generic.JsonSchemas {

  /** Timestamp schema */
  implicit lazy val timestampSchema: JsonSchema[Instant] =
    xmapJsonSchema[String, Instant](
      implicitly[JsonSchema[String]],
      it => ZonedDateTime.parse(it).toInstant,
      _.toString
    )
}
