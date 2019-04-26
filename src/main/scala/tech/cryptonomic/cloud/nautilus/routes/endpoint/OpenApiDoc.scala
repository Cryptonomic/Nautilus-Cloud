package tech.cryptonomic.cloud.nautilus.routes.endpoint

import endpoints.openapi
import endpoints.openapi.model.{Info, OpenApi}

object OpenApiDoc
    extends ApiKeyEndpoints
    with UserEndpoints
    with openapi.model.OpenApiSchemas
    with openapi.JsonSchemaEntities
    with openapi.BasicAuthentication {

  def openApi: OpenApi = openApi(Info("Nautilus-Cloud API", "0.0.1"))(
    getAllKeys,
    validateApiKey,
    createUser,
    updateUser,
    getUser,
    getUserKeys
  )

}
