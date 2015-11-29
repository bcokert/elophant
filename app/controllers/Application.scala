package controllers

import play.api.Logger
import play.api.mvc._

class Application extends Controller {
  def untrail(path: String) = Action { request =>
    Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
    MovedPermanently("/" + path)
  }
}
