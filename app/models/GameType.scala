package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class GameType(id: Int, name: String, description: String) extends DatabaseModel {
  def copyWithId(id: Int) = GameType(id, name, description)
}

object GameType {
  implicit val gameTypeReads: Reads[GameType] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "description" ).read[String]
  )(GameType(0, _, _))
  implicit val gameTypeWrites = Json.writes[GameType]
}


