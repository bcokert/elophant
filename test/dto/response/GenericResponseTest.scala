package dto.response

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
}
