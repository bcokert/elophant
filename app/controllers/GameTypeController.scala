package controllers

import dao.GameTypesDao
import dto.response.GenericSuccessResponse
import error.jsonErrorWrites
import models.GameType
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration._

class GameTypeController extends Controller {

  def getGameType(id: Int) = Action {
    Ok(Json.toJson(Await.result(GameTypesDao.getGameType(id), 5.seconds))).as("application/json")
  }

  def getGameTypes = Action {
    Ok(Json.toJson(Await.result(GameTypesDao.getGameTypes, 5.seconds).map(Json.toJson(_)))).as("application/json")
  }

  def addGameType() = Action(parse.json) { request =>
    Json.fromJson[GameType](request.body) match {
      case JsSuccess(gameType: GameType, _) =>
        Await.result(GameTypesDao.addGameType(gameType), 5.seconds)
        Ok(Json.obj(
          "success" -> JsBoolean(true)
        )).as("application/json")
      case err@JsError(_) =>
        Logger.error("Invalid Post Body for addGameType: " + Json.toJson(err))
        BadRequest(Json.obj(
          "success" -> JsBoolean(false),
          "reason" -> "Invalid Post Body",
          "errors" -> Json.toJson(err)
        )).as("application/json)")
    }
  }

  def deleteGameType(id: Int) = Action {
    Ok(Json.toJson(Await.result(GameTypesDao.deleteGameType(id), 5.seconds) match {
      case 1 => GenericSuccessResponse(true)
      case 0 => GenericSuccessResponse(false)
    })).as("application/json")
}
}
