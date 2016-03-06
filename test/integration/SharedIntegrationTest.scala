package integration

import dto.response.AccessControlFailureResponse
import play.api.libs.json._
import play.api.test.Helpers._
import slick.driver.PostgresDriver.api._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.jdbc.JdbcBackend
import types.{PermissionLevels, PermissionTypes}

import scala.concurrent.Future

class SharedIntegrationTest extends BaseIntegrationTest {

  override def resetDatabase(): Unit = {
    withDatabase { db =>
      waitFor(db.run(sqlu"DELETE FROM rating;"))
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

  "GET /gameType" should "return an empty list when no gameTypes exist" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ))
    testRequestAndVerify(GET, "/gameType") {
      Json.arr()
    }
  }

  it should "return all gameTypes if there are any" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ))
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO game_type(id, name, description) VALUES(4, 'foosball', 'foosball description');"))
      waitFor(db.run(sqlu"INSERT INTO game_type(id, name, description) VALUES(5, 'chess', 'chess description');"))

      testRequestAndManuallyVerify(GET, "/gameType") { results =>
        results.as[JsArray].value should contain(Json.obj(
          "id" -> 4,
          "name" -> "foosball",
          "description" -> "foosball description"
        ))

        results.as[JsArray].value should contain(Json.obj(
          "id" -> 5,
          "name" -> "chess",
          "description" -> "chess description"
        ))
      }
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.NONE))
    testRequestAndVerify(GET, "/gameType", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.GAME_TYPE, PermissionLevels.NONE, PermissionLevels.READ)))
    }
  }

  "POST /gameType" should "create a new gameType" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.CREATE))
    val data = Json.obj(
      "name" -> "foosball",
      "description" -> "that one with the red balls"
    )

    testRequestWithJsonAndManuallyVerify(POST, "/gameType", data) { result =>
      (result \ "name").get should equal(JsString("foosball"))
      (result \ "description").get should equal(JsString("that one with the red balls"))
    }

    testRequestAndManuallyVerify(GET, "/gameType") { results =>
      val res = results.as[JsArray].value
      res.size should equal(1)

      (res.head \ "name").get should equal(JsString("foosball"))
      (res.head \ "description").get should equal(JsString("that one with the red balls"))
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ))
    val data = Json.obj(
      "name" -> "foosball",
      "description" -> "that one with the red balls"
    )

    testRequestWithJsonAndVerify(POST, "/gameType", data, expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.GAME_TYPE, PermissionLevels.READ, PermissionLevels.CREATE)))
    }
  }

  it should "return a readable failure response when given bad json" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.CREATE))
    val data = Json.obj(
      "description" -> 1
    )

    testRequestWithJsonAndVerify(POST, "/gameType", data, expectedResponseCode = BAD_REQUEST) {
      Json.obj(
        "success" -> false,
        "errorReasons" -> Json.arr(
          "/description - error.expected.jsstring",
          "/name - error.path.missing"
        )
      )
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

      testRequestAndVerify(GET, "/gameType") {
        Json.arr()
      }
    }
  }

  it should "return a failure response if the gameType doesn't exist" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.DELETE))
    withDatabase { db =>
      testRequestAndVerify(DELETE, "/gameType/9") {
        Json.obj(
          "success" -> false,
          "errorReasons" -> Json.arr(
            "Game Type with id '9' does not exist"
          )
        )
      }
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.UPDATE))
    testRequestAndVerify(DELETE, "/gameType/42", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.GAME_TYPE, PermissionLevels.UPDATE, PermissionLevels.DELETE)))
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

  "GET /player" should "return an empty list when no players exist" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.READ))
    testRequestAndVerify(GET, "/player") {
      Json.arr()
    }
  }

  it should "return all players if there are any" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.READ))
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) VALUES(4, 'bob', 'smith', 'bob.smith@hotmail.com');"))
      waitFor(db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) VALUES(5, 'jim', 'thompson', 'jim@work.com');"))

      testRequestAndManuallyVerify(GET, "/player") { results =>
        results.as[JsArray].value should contain(Json.obj(
          "id" -> 4,
          "firstName" -> "bob",
          "lastName" -> "smith",
          "email" -> "bob.smith@hotmail.com"
        ))

        results.as[JsArray].value should contain(Json.obj(
          "id" -> 5,
          "firstName" -> "jim",
          "lastName" -> "thompson",
          "email" -> "jim@work.com"
        ))
      }
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.NONE))
    testRequestAndVerify(GET, "/player", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.PLAYER, PermissionLevels.NONE, PermissionLevels.READ)))
    }
  }

  "POST /player" should "create a new player" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.CREATE))
    val data = Json.obj(
      "firstName" -> "johnny",
      "lastName" -> "pinkerton",
      "email" -> "john.pinkerton@work.com"
    )
    testRequestWithJsonAndManuallyVerify(POST, "/player", data) { result =>
      (result \ "firstName").get should equal(JsString("johnny"))
      (result \ "lastName").get should equal(JsString("pinkerton"))
      (result \ "email").get should equal(JsString("john.pinkerton@work.com"))
    }

    testRequestAndManuallyVerify(GET, "/player") { results =>
      val res = results.as[JsArray].value
      res.size should equal(1)

      (res.head \ "firstName").get should equal(JsString("johnny"))
      (res.head \ "lastName").get should equal(JsString("pinkerton"))
      (res.head \ "email").get should equal(JsString("john.pinkerton@work.com"))
    }
  }

  it should "return a readable failure response when given bad json" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.CREATE))
    val data = Json.obj(
      "firstName" -> "johnny",
      "email" -> 14
    )

    testRequestWithJsonAndVerify(POST, "/player", data, defaultHeaders, BAD_REQUEST) {
      Json.obj(
        "success" -> false,
        "errorReasons" -> Json.arr(
          "/email - error.expected.jsstring",
          "/lastName - error.path.missing"
        )
      )
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.READ))
    val data = Json.obj(
      "firstName" -> "johnny",
      "lastName" -> "pinkerton",
      "email" -> "john.pinkerton@work.com"
    )
    testRequestWithJsonAndVerify(POST, "/player", data, expectedResponseCode = FORBIDDEN) {
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

      testRequestAndVerify(GET, "/player") {
        Json.arr()
      }
    }
  }

  it should "return a failure response if the player doesn't exist" in {
    setAppPermissions(Map(PermissionTypes.PLAYER -> PermissionLevels.DELETE))
    withDatabase { db =>
      testRequestAndVerify(DELETE, "/player/9") {
        Json.obj(
          "success" -> false,
          "errorReasons" -> Json.arr(
            "Player with id '9' does not exist"
          )
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




  private def setupRatingTests(db: JdbcBackend#DatabaseDef): Unit = {
    waitFor(Future.sequence(Seq(
      db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) values(1, 'test1first', 'test1last', 'test1@test.com');"),
      db.run(sqlu"INSERT INTO player(id, first_name, last_name, email) values(2, 'test2first', 'test2last', 'test2@test.com');"),
      db.run(sqlu"INSERT INTO game_type(id, name, description) values(1, 'testGameType1', 'Test Game Type 1');"),
      db.run(sqlu"INSERT INTO game_type(id, name, description) values(2, 'testGameType2', 'Test Game Type 2');")
    )))
    waitFor(Future.sequence(Seq(
      db.run(sqlu"INSERT INTO rating(elo_rating, player_id, game_type_id) values(1111, 1, 1);"),
      db.run(sqlu"INSERT INTO rating(elo_rating, player_id, game_type_id) values(1122, 1, 2);"),
      db.run(sqlu"INSERT INTO rating(elo_rating, player_id, game_type_id) values(2211, 2, 1);"),
      db.run(sqlu"INSERT INTO rating(elo_rating, player_id, game_type_id) values(2222, 2, 2);")
    )))
  }

  "GET /eloRatings" should "return all ratings when no query params are sent" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.READ))
    withDatabase { db =>
      setupRatingTests(db)

      testRequestAndManuallyVerify(GET, "/eloRating") { results =>
        val jsResults = results.as[JsArray].value

        jsResults should contain(Json.obj(
          "rating" -> 1111,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 1122,
          "playerId" -> 1,
          "gameTypeId" -> 2
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2211,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2222,
          "playerId" -> 2,
          "gameTypeId" -> 2
        ))
      }
    }
  }

  it should "return only ratings by player id when a player id is requested" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.READ))
    withDatabase { db =>
      setupRatingTests(db)

      testRequestAndManuallyVerify(GET, "/eloRating?playerId=2") { results =>
        val jsResults = results.as[JsArray].value

        jsResults should contain(Json.obj(
          "rating" -> 2211,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2222,
          "playerId" -> 2,
          "gameTypeId" -> 2
        ))
      }
    }
  }

  it should "return only ratings by game type id when a game type id is requested" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.READ))
    withDatabase { db =>
      setupRatingTests(db)

      testRequestAndManuallyVerify(GET, "/eloRating?gameTypeId=1") { results =>
        val jsResults = results.as[JsArray].value

        jsResults should contain(Json.obj(
          "rating" -> 1111,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2211,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))
      }
    }
  }

  it should "return only 1 rating when game type id and player id are requested" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.READ))
    withDatabase { db =>
      setupRatingTests(db)

      testRequestAndVerify(GET, "/eloRating?playerId=2&gameTypeId=1") {
        Json.obj(
          "rating" -> 2211,
          "playerId" -> 2,
          "gameTypeId" -> 1
        )
      }
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.NONE))
    testRequestAndVerify(GET, "/eloRating", expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.RATING, PermissionLevels.NONE, PermissionLevels.READ)))
    }
  }

  "POST /gameResult" should "Update the ratings of two players after a series of results, and not affect other ratings" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.UPDATE))
    withDatabase { db =>
      setupRatingTests(db)

      val gameResult = Json.obj(
        "player1Id" -> 1,
        "player2Id" -> 2,
        "score" -> 1,
        "gameTypeId" -> 1
      )

      testRequestWithJsonAndVerify(POST, "/gameResult", gameResult) {
        Json.arr(Json.obj(
          "rating" -> 1127,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ), Json.obj(
          "rating" -> 2195,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))
      }

      testRequestAndManuallyVerify(GET, "/eloRating") { results =>
        val jsResults = results.as[JsArray].value

        jsResults should contain(Json.obj(
          "rating" -> 1127,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 1122,
          "playerId" -> 1,
          "gameTypeId" -> 2
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2195,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2222,
          "playerId" -> 2,
          "gameTypeId" -> 2
        ))
      }

      testRequestWithJsonAndVerify(POST, "/gameResult", gameResult) {
        Json.arr(Json.obj(
          "rating" -> 1143,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ), Json.obj(
          "rating" -> 2179,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))
      }

      testRequestAndManuallyVerify(GET, "/eloRating") { results =>
        val jsResults = results.as[JsArray].value

        jsResults should contain(Json.obj(
          "rating" -> 1143,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 1122,
          "playerId" -> 1,
          "gameTypeId" -> 2
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2179,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2222,
          "playerId" -> 2,
          "gameTypeId" -> 2
        ))
      }

      val gameResult2 = Json.obj(
        "player1Id" -> 1,
        "player2Id" -> 2,
        "score" -> 0,
        "gameTypeId" -> 1
      )

      testRequestWithJsonAndVerify(POST, "/gameResult", gameResult2) {
        Json.arr(Json.obj(
          "rating" -> 1143,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ), Json.obj(
          "rating" -> 2179,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))
      }

      testRequestAndManuallyVerify(GET, "/eloRating") { results =>
        val jsResults = results.as[JsArray].value

        jsResults should contain(Json.obj(
          "rating" -> 1143,
          "playerId" -> 1,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 1122,
          "playerId" -> 1,
          "gameTypeId" -> 2
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2179,
          "playerId" -> 2,
          "gameTypeId" -> 1
        ))

        jsResults should contain(Json.obj(
          "rating" -> 2222,
          "playerId" -> 2,
          "gameTypeId" -> 2
        ))
      }
    }
  }

  it should "return a readable failure response when given bad json" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.UPDATE))
    val data = Json.obj(
      "player1Id" -> 1,
      "score" -> "1"
    )

    testRequestWithJsonAndVerify(POST, "/gameResult", data, defaultHeaders, BAD_REQUEST) {
      Json.obj(
        "success" -> false,
        "errorReasons" -> Json.arr(
          "/gameTypeId - error.path.missing",
          "/player2Id - error.path.missing",
          "/score - error.expected.jsnumber"
        )
      )
    }
  }

  it should "fail if you don't have the necessary permissions" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.CREATE))

    val gameResult2 = Json.obj(
      "player1Id" -> 1,
      "player2Id" -> 2,
      "score" -> 0,
      "gameTypeId" -> 1
    )

    testRequestWithJsonAndVerify(POST, "/gameResult", gameResult2, expectedResponseCode = FORBIDDEN) {
      Json.arr(Json.toJson(AccessControlFailureResponse(PermissionTypes.RATING, PermissionLevels.CREATE, PermissionLevels.UPDATE)))
    }
  }

  it should "fail if player 1 does not exist" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.UPDATE))
    withDatabase { db =>
      setupRatingTests(db)

      val gameResult = Json.obj(
        "player1Id" -> 932457,
        "player2Id" -> 2,
        "score" -> 1,
        "gameTypeId" -> 1
      )

      testRequestWithJsonAndVerify(POST, "/gameResult", gameResult, expectedResponseCode = BAD_REQUEST) {
        Json.obj(
          "success" -> false,
          "errorReasons" -> Json.arr(
            "Rating for Player '932457' and Game Type '1' does not exist"
          )
        )
      }
    }
  }

  it should "fail if player 2 does not exist" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.UPDATE))
    withDatabase { db =>
      setupRatingTests(db)

      val gameResult = Json.obj(
        "player1Id" -> 1,
        "player2Id" -> 244,
        "score" -> 1,
        "gameTypeId" -> 1
      )

      testRequestWithJsonAndVerify(POST, "/gameResult", gameResult, expectedResponseCode = BAD_REQUEST) {
        Json.obj(
          "success" -> false,
          "errorReasons" -> Json.arr(
            "Rating for Player '244' and Game Type '1' does not exist"
          )
        )
      }
    }
  }

  it should "fail if both players do not exist" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.UPDATE))
    withDatabase { db =>
      setupRatingTests(db)

      val gameResult = Json.obj(
        "player1Id" -> 99,
        "player2Id" -> 244,
        "score" -> 1,
        "gameTypeId" -> 1
      )

      testRequestWithJsonAndVerify(POST, "/gameResult", gameResult, expectedResponseCode = BAD_REQUEST) {
        Json.obj(
          "success" -> false,
          "errorReasons" -> Json.arr(
            "Rating for Player '99' and Game Type '1' does not exist",
            "Rating for Player '244' and Game Type '1' does not exist"
          )
        )
      }
    }
  }

  it should "fail if the game type does not exist" in {
    setAppPermissions(Map(PermissionTypes.RATING -> PermissionLevels.UPDATE))
    withDatabase { db =>
      setupRatingTests(db)

      val gameResult = Json.obj(
        "player1Id" -> 1,
        "player2Id" -> 2,
        "score" -> 1,
        "gameTypeId" -> 9563
      )

      testRequestWithJsonAndVerify(POST, "/gameResult", gameResult, expectedResponseCode = BAD_REQUEST) {
        Json.obj(
          "success" -> false,
          "errorReasons" -> Json.arr(
            "Rating for Player '1' and Game Type '9563' does not exist",
            "Rating for Player '2' and Game Type '9563' does not exist"
          )
        )
      }
    }
  }




  "A NotFound error" should "be returned if the requested resource is not found" in {
    withDatabase { db =>
      testRequestAndManuallyVerify(GET, "/thisIsntHere", defaultHeaders, NOT_FOUND) { result =>
        result.value should equal(Json.obj(
          "success" -> false,
          "errorReasons" -> Json.arr(
            "The requested resource does not exist."
          )
        ))
      }
    }
  }
}
