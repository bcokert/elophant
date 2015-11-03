package models

import play.api.libs.json._
import play.api.libs.json.Reads._

case class League(id: Int, name: String)

object League {
  implicit val leagueReads = (JsPath \ "name").read[String].map(name => League(0, name))
  implicit val leagueWrites = Json.writes[League]
}

