package model

import models.GameType
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class GameTypeTest extends FlatSpec with Matchers {
   "a GameType" should "serialize into json" in {
     val gameType = GameType(21, "Foosball", "that game with the balls")
     Json.toJson(gameType) should equal(Json.obj(
       "id" -> 21,
       "name" -> "Foosball",
       "description" -> "that game with the balls"
     ))
   }

   it should "deserialize from json" in {
     val json = Json.obj(
       "name" -> "Foosball",
       "description" -> "that game with the balls"
     )

     Json.fromJson[GameType](json) should equal(JsSuccess(GameType(
       0,
       "Foosball",
       "that game with the balls"
     )))
   }

   it should "ignore an id if given when deserializing" in {
     val json = Json.obj(
       "id" -> 44,
       "name" -> "Foosball",
       "description" -> "that game with the balls"
     )

     Json.fromJson[GameType](json) should equal(JsSuccess(GameType(
       0,
       "Foosball",
       "that game with the balls"
     )))
   }
 }
