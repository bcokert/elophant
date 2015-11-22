package integration

import play.api.libs.json._
import play.api.test.Helpers._
import slick.driver.PostgresDriver.api._

class GameTypeIntegrationTest extends BaseIntegrationTest {

  override def resetDatabase(): Unit = {
    withDatabase { db =>
      waitFor(db.run(sqlu"DELETE FROM game_type;"))
    }
  }

  "GET /gameType/$id" should "return the gameType" in {
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO game_type(id, name, description) VALUES(7, 'foosball', 'That game with the balls');"))

      testRequestAndVerify(GET, "/gameType/7") {
        Json.obj(
          "id" -> 7,
          "name" -> "foosball",
          "description" -> "That game with the balls"
        )
      }
    }
  }

  "GET /gameType/" should "return an empty list when no gameTypes exist" in {
    testRequestAndVerify(GET, "/gameType/") {
      Json.arr()
    }
  }

  it should "return all gameTypes if there are any" in {
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO game_type(id, name, description) VALUES(4, 'foosball', 'foosball description');"))
      waitFor(db.run(sqlu"INSERT INTO game_type(id, name, description) VALUES(5, 'chess', 'chess description');"))

      testRequestAndVerify(GET, "/gameType/") {
        Json.arr(
          Json.obj(
            "id" -> 4,
            "name" -> "foosball",
            "description" -> "foosball description"
          ),
          Json.obj(
            "id" -> 5,
            "name" -> "chess",
            "description" -> "chess description"
          )
        )
      }
    }
  }

  "POST /gameType/" should "create a new gameType" in {
    val data = Json.obj(
      "name" -> "foosball",
      "description" -> "that one with the red balls"
    )
    testRequestWithJsonAndVerify(POST, "/gameType/", data) {
      Json.obj("success" -> true)
    }

    testRequestAndManuallyVerify(GET, "/gameType/") { results =>
      val res = results.as[JsArray].value
      res.size should equal(1)

      (res.head \ "name").get should equal(JsString("foosball"))
      (res.head \ "description").get should equal(JsString("that one with the red balls"))
    }
  }

  "DELETE /gameType/$id" should "delete the gameType with the given id" in {
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO game_type(id, name, description) VALUES(3, 'go', 'too hard');"))

      testRequestAndVerify(DELETE, s"/gameType/3") {
        Json.obj(
          "success" -> true
        )
      }

      testRequestAndVerify(GET, "/gameType/") {
        Json.arr()
      }
    }
  }

  it should "return a failure response if the gameType doesn't exist" in {
    withDatabase { db =>
      testRequestAndVerify(DELETE, "/gameType/9") {
        Json.obj(
          "success" -> false
        )
      }
    }
  }
}
