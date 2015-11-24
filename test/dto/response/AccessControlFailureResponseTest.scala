package dto.response

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._
import types.PermissionTypes
import types.PermissionLevels

class AccessControlFailureResponseTest extends FlatSpec with Matchers {
  "an AccessControlFailureResponse" should "serialize into json" in {
    val response = AccessControlFailureResponse(PermissionTypes.PLAYER, PermissionLevels.READ, PermissionLevels.UPDATE, Some("Due to stuff"))
    Json.toJson(response) should equal(Json.obj(
      "message" -> s"You do not have access to this resource. You have 'READ', but require 'UPDATE' or higher.",
      "permissionType" -> "PLAYER",
      "currentLevel" -> "READ",
      "minimumLevel" -> "UPDATE",
      "details" -> "Due to stuff"
    ))
  }
}
