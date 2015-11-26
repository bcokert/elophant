package integration

import dto.response.AccessControlFailureResponse
import play.api.libs.json._
import play.api.test.Helpers._
import slick.driver.PostgresDriver.api._
import types.{PermissionLevels, PermissionTypes}

class SharedIntegrationTest extends BaseIntegrationTest {

  override def resetDatabase(): Unit = {
    withDatabase { db =>
      waitFor(db.run(sqlu"DELETE FROM player;"))
      waitFor(db.run(sqlu"DELETE FROM game_type;"))
    }
  }





  "GET /gameType/$id" should "return the gameType" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ))
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

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.NONE))
    testRequestAndVerify(GET, "/gameType/33", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.GAME_TYPE, PermissionLevels.NONE, PermissionLevels.READ)))
    }
  }

  "GET /gameType/" should "return an empty list when no gameTypes exist" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ))
    testRequestAndVerify(GET, "/gameType/") {
      Json.arr()
    }
  }

  it should "return all gameTypes if there are any" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ))
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
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.CREATE))
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
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.DELETE))
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
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.DELETE))
    withDatabase { db =>
      testRequestAndVerify(DELETE, "/gameType/9") {
        Json.obj(
          "success" -> false
        )
      }
    }
  }





  "GET /player/$id" should "return the player" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.READ))
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

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.NONE))
    testRequestAndVerify(GET, "/player/19", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.PLAYER, PermissionLevels.NONE, PermissionLevels.READ)))
    }
  }

  "GET /player/" should "return an empty list when no players exist" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.READ))
    testRequestAndVerify(GET, "/player/") {
      Json.arr()
    }
  }

  it should "return all players if there are any" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.READ))
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

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.NONE))
    testRequestAndVerify(GET, "/player/", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.PLAYER, PermissionLevels.NONE, PermissionLevels.READ)))
    }
  }

  "POST /player/" should "create a new player" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.CREATE))
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

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.READ))
    val data = Json.obj(
      "firstName" -> "johnny",
      "lastName" -> "pinkerton",
      "email" -> "john.pinkerton@work.com"
    )
    testRequestWithJsonAndVerify(POST, "/player/", data, expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.PLAYER, PermissionLevels.READ, PermissionLevels.CREATE)))
    }
  }

  "DELETE /player/$id" should "delete the player with the given id" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.DELETE))
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
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.DELETE))
    withDatabase { db =>
      testRequestAndVerify(DELETE, "/player/9") {
        Json.obj(
          "success" -> false
        )
      }
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.UPDATE))
    testRequestAndVerify(DELETE, "/player/19", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.PLAYER, PermissionLevels.UPDATE, PermissionLevels.DELETE)))
    }
  }
}
