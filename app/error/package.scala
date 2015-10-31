package object error {
  import play.api.libs.json._

  implicit object jsonErrorWrites extends Writes[JsError] {
    def writes(o: JsError): JsValue = Json.obj(
      "errors" -> JsArray(
        o.errors.map {
          case (path, validationErrors) => Json.obj(
            "path" -> Json.toJson(path.toString()),
            "validationErrors" -> JsArray(validationErrors.map(validationError => Json.obj(
              "message" -> JsString(validationError.message),
              "args" -> JsArray(validationError.args.map {
                case x: Int => JsNumber(x)
                case x => JsString(x.toString)
              })
            )))
          )
        }
      )
    )
  }
}
