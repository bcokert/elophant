package integration

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.Logger
import slick.driver.PostgresDriver.api._

class PlayersIntegrationTest extends BaseIntegrationTest {

  override def resetDatabase(): Unit = {
    withDatabase { db =>
      waitFor(db.run(sqlu"DELETE FROM player;"))
    }
  }

  "GET /player/$id" should "return the player" in {
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) VALUES(7, 'bob', 'smith', 'bob.smith@hotmail.com');"))

      testRequestAndVerify(GET, "/player/7") {
        Json.obj(
          "id" -> 7,
          "firstName" -> "bob",
          "lastName" -> "smith",
          "email" -> "bob.smith@hotmail.com"
        )
      }
    }
  }

  "GET /player/" should "return an empty list when no players exist" in {
    testRequestAndVerify(GET, "/player/") {
      Json.arr()
    }
  }

  it should "return all players if there are any" in {
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) VALUES(4, 'bob', 'smith', 'bob.smith@hotmail.com');"))
      waitFor(db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) VALUES(5, 'jim', 'thompson', 'jim@work.com');"))

      testRequestAndVerify(GET, "/player/") {
        Json.arr(
          Json.obj(
            "id" -> 4,
            "firstName" -> "bob",
            "lastName" -> "smith",
            "email" -> "bob.smith@hotmail.com"
          ),
          Json.obj(
            "id" -> 5,
            "firstName" -> "jim",
            "lastName" -> "thompson",
            "email" -> "jim@work.com"
          )
        )
      }
    }
  }

  "POST /player/" should "create a new player" in {
    val data = Json.obj(
      "firstName" -> "johnny",
      "lastName" -> "pinkerton",
      "email" -> "john.pinkerton@work.com"
    )
    testRequestWithJsonAndVerify(POST, "/player/", data) {
      Json.obj("success" -> true)
    }

    testRequestAndManuallyVerify(GET, "/player/") { results =>
      val res = results.as[JsArray].value
      res.size should equal(1)

      (res.head \ "firstName").get should equal(JsString("johnny"))
      (res.head \ "lastName").get should equal(JsString("pinkerton"))
      (res.head \ "email").get should equal(JsString("john.pinkerton@work.com"))
    }
  }

  "DELETE /player/$id" should "delete the player with the given id" in {
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) VALUES(3, 'bob', 'smith', 'bob.smith@hotmail.com');"))

      testRequestAndVerify(DELETE, s"/player/3") {
        Json.obj(
          "success" -> true
        )
      }

      testRequestAndVerify(GET, "/player/") {
        Json.arr()
      }
    }
  }

  it should "return a failure response if the player doesn't exist" in {
    withDatabase { db =>
      testRequestAndVerify(DELETE, "/player/9") {
        Json.obj(
          "success" -> false
        )
      }
    }
  }
}
