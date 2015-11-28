package dto.response

import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.Logger

case class GenericResponse(success: Boolean, errorCodes: Option[Seq[Int]] = None, errorReasons: Option[Seq[String]] = None)

object GenericResponse {
  implicit val writes = Json.writes[GenericResponse]

  def fromJsonErrors(errors: Seq[(JsPath, Seq[ValidationError])]): GenericResponse = GenericResponse(
    success = false,
    None,
    Some(errors.flatMap {
      case (path, validationErrors) => validationErrors.map(error => s"$path - ${error.messages.mkString(", ")}")
    }.sorted)
  )
}
