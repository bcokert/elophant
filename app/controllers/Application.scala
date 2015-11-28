package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def untrail(path: String) = Action {
    MovedPermanently("/" + path)
  }
}
