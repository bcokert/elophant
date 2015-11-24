package dto.response

import play.api.libs.json._
import types.PermissionLevel
import types.PermissionType

case class AccessControlFailureResponse(permissionType: PermissionType, currentLevel: PermissionLevel, minimumLevel: PermissionLevel, details: Option[String] = None)

object AccessControlFailureResponse {
  implicit val accessControlWrites = new Writes[AccessControlFailureResponse] {
    def writes(response: AccessControlFailureResponse): JsValue = {
      Json.obj(
        "message" -> s"You do not have access to this resource. You have '${response.currentLevel}', but require '${response.minimumLevel}' or higher.",
        "permissionType" -> response.permissionType.toString,
        "currentLevel" -> response.currentLevel.toString,
        "minimumLevel" -> response.minimumLevel.toString,
        "details" -> JsString(response.details.getOrElse(""))
      )
    }
  }
}
