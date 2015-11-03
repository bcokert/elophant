package model

import models.Player
import play.api.libs.json._
import org.scalatest.{FlatSpec, Matchers}

class PlayerTest extends FlatSpec with Matchers {
  "a Player" should "serialize into json" in {
    val player = Player(21, "Bob", "Marley", "bob.marley@website.com")
    Json.toJson(player) should equal(Json.obj(
      "id" -> 21,
      "firstName" -> "Bob",
      "lastName" -> "Marley",
      "email" -> "bob.marley@website.com"
    ))
  }

  it should "deserialize from json" in {
    val json = Json.obj(
      "firstName" -> "Bob",
      "lastName" -> "Marley",
      "email" -> "bob.marley@website.com"
    )

    Json.fromJson[Player](json) should equal(JsSuccess(Player(
      0,
      "Bob",
      "Marley",
      "bob.marley@website.com"
    )))
  }

  it should "ignore an id if given when deserializing" in {
    val json = Json.obj(
      "id" -> 44,
      "firstName" -> "Bob",
      "lastName" -> "Marley",
      "email" -> "bob.marley@website.com"
    )

    Json.fromJson[Player](json) should equal(JsSuccess(Player(
      0,
      "Bob",
      "Marley",
      "bob.marley@website.com"
    )))
  }
}
