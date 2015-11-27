package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import types.Rating

case class EloRating(id: Int, rating: Rating, playerId: Int, gameTypeId: Int)

object EloRating {
  implicit val eloRatingWrites = new Writes[EloRating] {
    def writes(r: EloRating): JsValue = {
      Json.obj(
        "rating" -> r.rating,
        "playerId" -> r.playerId,
        "gameTypeId" -> r.gameTypeId
      )
    }
  }

  implicit val eloRatingReads: Reads[EloRating] = (
    (JsPath \ "rating").read[Rating] and
    (JsPath \ "playerId").read[Int] and
    (JsPath \ "gameTypeId").read[Int]
    )(EloRating(0, _, _, _))
}
