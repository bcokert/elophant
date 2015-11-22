package models

import play.api.libs.json._
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
}
