package model

import models.GameResult
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class GameResultTest extends FlatSpec with Matchers {
   "a GameResult" should "serialize into json" in {
     val gameResult = GameResult(21, 55, didPlayer1Win = true, 1)
     Json.toJson(gameResult) should equal(Json.obj(
       "player1Id" -> 21,
       "player2Id" -> 55,
       "didPlayer1Win" -> true,
       "gameTypeId" -> 1
     ))
   }

   it should "deserialize from json" in {
     val json = Json.obj(
       "player1Id" -> 21,
       "player2Id" -> 55,
       "didPlayer1Win" -> true,
       "gameTypeId" -> 1
     )

     Json.fromJson[GameResult](json) should equal(JsSuccess(GameResult(21, 55, didPlayer1Win = true, 1)))
   }
 }
