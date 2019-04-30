package tech.cryptonomic.cloud.nautilus.routes.endpoint

import endpoints.akkahttp.server
import endpoints.openapi.model

/** Documentation object */
object Docs extends server.Endpoints with model.OpenApiSchemas with server.JsonSchemaEntities {

  /** OpenAPI documentation route */
  val route = endpoint(
    request = get(
      url = path / "openapi.json"
    ),
    response = jsonResponse[model.OpenApi]()
  ).implementedBy(_ => OpenApiDoc.openApi)
}
