package dto.response

import play.api.libs.json._

case class GenericSuccessResponse(success: Boolean)

object GenericSuccessResponse {
  implicit val writes = Json.writes[GenericSuccessResponse]
}
