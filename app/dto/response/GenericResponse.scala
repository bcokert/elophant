package dto.response

import play.api.libs.json._

case class GenericResponse(success: Boolean, errorCodes: Option[Seq[Int]] = None, errorReasons: Option[Seq[String]] = None)

object GenericResponse {
  implicit val writes = Json.writes[GenericResponse]
}
