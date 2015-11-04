package model

import models.EloRating
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class EloRatingTest extends FlatSpec with Matchers {
   "an EloRating" should "serialize into json" in {
     val eloRating = EloRating(0, 1322, 1, 4, 3)
     Json.toJson(eloRating) should equal(Json.obj(
       "rating" -> 1322,
       "playerId" -> 1,
       "leagueId" -> 4,
       "gameTypeId" -> 3
     ))
   }
 }
