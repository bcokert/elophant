package controllers

import models.{GenericIsSuccess, Player}
import play.api.mvc._
import play.api.Logger
import play.api.libs.json._
import dao.Players
import error.jsonErrorWrites

import scala.concurrent.Await
import scala.concurrent.duration._

class PlayerController extends Controller {

  def getPlayer(id: Int) = Action {
    Ok(Json.toJson(Await.result(Players.getPlayer(id), 5.seconds))).as("application/json")
  }

  def getPlayers = Action {
    Ok(Json.toJson(Await.result(Players.getPlayers, 5.seconds).map(Json.toJson(_)))).as("application/json")
  }

  def addPlayer() = Action(parse.json) { request =>
    Json.fromJson[Player](request.body) match {
      case JsSuccess(p: Player, _) =>
        Await.result(Players.addPlayer(p), 5.seconds)
        Ok(Json.obj(
          "success" -> JsBoolean(true)
        )).as("application/json")
      case err@JsError(_) =>
        Logger.error("Invalid Post Body for addPlayer: " + Json.toJson(err))
        BadRequest(Json.obj(
          "success" -> JsBoolean(false),
          "reason" -> "Invalid Post Body",
          "errors" -> Json.toJson(err)
        )).as("application/json)")
    }
  }

  def deletePlayer(id: Int) = Action {
    Ok(Json.toJson(Await.result(Players.deletePlayer(id), 5.seconds) match {
      case 1 => GenericIsSuccess(true)
      case 0 => GenericIsSuccess(false)
    })).as("application/json")
}
}
