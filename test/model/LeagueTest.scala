package model

import models.League
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class LeagueTest extends FlatSpec with Matchers {
   "a League" should "serialize into json" in {
     val league = League(21, "Foosball")
     Json.toJson(league) should equal(Json.obj(
       "id" -> 21,
       "name" -> "Foosball"
     ))
   }

   it should "deserialize from json" in {
     val json = Json.obj(
       "name" -> "Foosball"
     )

     Json.fromJson[League](json).get should equal(League(
       0,
       "Foosball"
     ))
   }

   it should "ignore an id if given when deserializing" in {
     val json = Json.obj(
       "id" -> 44,
       "name" -> "Foosball"
     )

     Json.fromJson[League](json).get should equal(League(
       0,
       "Foosball"
     ))
   }
 }
