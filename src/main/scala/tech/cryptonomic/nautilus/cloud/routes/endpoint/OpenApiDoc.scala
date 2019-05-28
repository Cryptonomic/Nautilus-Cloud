package tech.cryptonomic.nautilus.cloud.routes.endpoint

import endpoints.algebra.Documentation
import endpoints.openapi
import endpoints.openapi.model.{Info, MediaType, OpenApi, Schema}

/** OpenAPI documentation definition */
object OpenApiDoc
    extends ApiKeyEndpoints
    with UserEndpoints
    with openapi.model.OpenApiSchemas
    with openapi.JsonSchemaEntities {

  /** OpenAPI definition */
  def openApi: OpenApi = openApi(Info("Nautilus-Cloud API", "0.0.1"))(
    getAllKeys,
    validateApiKey,
    createUser,
    updateUser,
    getUser,
    getUserKeys,
    getApiKeyUsage
  )

  override def created[A](
      response: List[OpenApiDoc.DocumentedResponse],
      invalidDocs: Documentation
  ): List[OpenApiDoc.DocumentedResponse] =
    response :+ OpenApiDoc.DocumentedResponse(
          status = 201,
          documentation = invalidDocs.getOrElse(""),
          content = Map(
            "application/json" -> MediaType(schema = Some(Schema.Array(Schema.simpleInteger, None)))
          )
        )
}
