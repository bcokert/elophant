package models

import play.api.libs.json._

case class GenericIsSuccess(success: Boolean)

object GenericIsSuccess {
  implicit val reads = Json.reads[GenericIsSuccess]
  implicit val writes = Json.writes[GenericIsSuccess]
}
