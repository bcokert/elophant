package integration

import dao.PlayersDao
import models.Player
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.{Play, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.duration._
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._

class PlayersIntegrationTest extends BaseIntegrationTest {

  override def resetDatabase(): Unit = {
    withDatabase { db =>
      waitFor(db.run(sqlu"DELETE FROM player;"))
    }
  }

  "GET /player/" should "return an empty list when no players exist" in {
    val response = testRequest(GET, "/player/")
    status(response) should equal(200)
    contentAsJson(response) should equal(Json.arr())
  }

  it should "return all players if there are any" in {
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO player(first_name, last_name, email) VALUES('bob', 'smith', 'bob.smith@hotmail.com');"))
      waitFor(db.run(sqlu"INSERT INTO player(first_name, last_name, email) VALUES('jim', 'thompson', 'jim@work.com');"))

      val response = testRequest(GET, "/player/")
      status(response) should equal(200)

      val results = contentAsJson(response).as[JsArray].value

      results.size should equal(2)

      (results.head \ "firstName").get should equal(JsString("bob"))
      (results.head \ "lastName").get should equal(JsString("smith"))
      (results.head \ "email").get should equal(JsString("bob.smith@hotmail.com"))

      (results(1) \ "firstName").get should equal(JsString("jim"))
      (results(1) \ "lastName").get should equal(JsString("thompson"))
      (results(1) \ "email").get should equal(JsString("jim@work.com"))
    }
  }
}
