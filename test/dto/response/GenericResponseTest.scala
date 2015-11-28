package dto.response

import models.Player
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class GenericResponseTest extends FlatSpec with Matchers {
  "a GenericResponse" should "serialize into json for a failure result" in {
    val response = GenericResponse(success = false, Some(Seq(322)), Some(Seq("Error occurred")))
    Json.toJson(response) should equal(Json.obj(
      "success" -> false,
      "errorCodes" -> Json.arr(
        322
      ),
      "errorReasons" -> Json.arr(
        "Error occurred"
      )
    ))
  }

  it should "serialize into json for a successful result" in {
    val response = GenericResponse(success = true)
    Json.toJson(response) should equal(Json.obj(
      "success" -> true
    ))
  }

  it should "display good looking errors for json errors, in sorted order" in {
    val json = Json.obj("firstName" -> false, "lastName" -> 1)
    Json.fromJson[Player](json) match {
      case JsSuccess(_, _) => fail("Somehow the broken json was parsed correctly")
      case JsError(e) =>
        val errorResponse = GenericResponse.fromJsonErrors(e)
        errorResponse should equal(GenericResponse(success = false, None, Some(Seq(
          "/email - error.path.missing",
          "/firstName - error.expected.jsstring",
          "/lastName - error.expected.jsstring"
        ))))
    }
  }
}
