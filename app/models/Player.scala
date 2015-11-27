package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Player(id: Int, firstName: String, lastName: String, email: String) extends DatabaseModel {
  def copyWithId(id: Int) = Player(id, firstName, lastName, email)
}

object Player {
  implicit val playerReads: Reads[Player] = (
    (JsPath \ "firstName").read[String] and
    (JsPath \ "lastName" ).read[String] and
    (JsPath \ "email").read[String]
  )(Player(0, _, _, _))
  implicit val playerWrites = Json.writes[Player]
}
