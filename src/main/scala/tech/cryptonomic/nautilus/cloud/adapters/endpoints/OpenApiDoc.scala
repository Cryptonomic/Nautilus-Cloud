package tech.cryptonomic.nautilus.cloud.adapters.endpoints

import endpoints.algebra.Documentation
import endpoints.openapi
import endpoints.openapi.model.{Info, MediaType, OpenApi, Schema}
import tech.cryptonomic.nautilus.cloud.application.domain.authentication.AuthorizationService.Permission

/** OpenAPI documentation definition */
object OpenApiDoc
    extends ApiKeyEndpoints
    with UserEndpoints
    with ResourceEndpoints
    with openapi.model.OpenApiSchemas
    with openapi.JsonSchemaEntities {

  /** OpenAPI definition */
  def openApi: OpenApi = openApi(Info("Nautilus-Cloud API", "0.0.1"))(
    getAllKeys,
    validateApiKey,
    updateUser,
    getUser,
    getUserKeys,
    getApiKeyUsage,
    getResourceEndpoint,
    listResourcesEndpoint,
    createResourceEndpoint,
    getCurrentUser,
    getCurrentUserKeys,
    getAllKeysForEnv
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

  /** Extension for using Conflict status code */
  override def conflict[A](response: Response[A], notFoundDocs: Documentation): Response[Option[A]] =
    DocumentedResponse(409, notFoundDocs.getOrElse(""), content = Map.empty) :: response

  /** Extension for using Forbidden status code */
  override def forbidden[A](
      response: List[OpenApiDoc.DocumentedResponse],
      invalidDocs: Documentation
  ): Response[Permission[A]] =
    DocumentedResponse(403, invalidDocs.getOrElse(""), content = Map.empty) :: response

  /** Extension for using Bad request status code */
  override def badRequest[A](
      response: List[OpenApiDoc.DocumentedResponse],
      invalidDocs: Documentation
  ): List[OpenApiDoc.DocumentedResponse] =
    DocumentedResponse(401, invalidDocs.getOrElse(""), content = Map.empty) :: response
}
