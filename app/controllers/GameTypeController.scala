package controllers

import dao.GameTypesDao
import dto.response.GenericResponse
import models.GameType
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import types.{PermissionLevels, PermissionTypes}

import scala.concurrent.Await
import scala.concurrent.duration._

class GameTypeController extends Controller with AccessControl {

  def getGameType(id: Int) = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ)) {
    Action {
      Ok(Json.toJson(Await.result(GameTypesDao.getGameType(id), 5.seconds))).as("application/json")
    }
  }

  def getGameTypes = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ)) {
    Action {
      Ok(Json.toJson(Await.result(GameTypesDao.getGameTypes, 5.seconds).map(Json.toJson(_)))).as("application/json")
    }
  }

  def addGameType() = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.CREATE)) {
    Action(parse.json) { request =>
      Json.fromJson[GameType](request.body) match {
        case JsSuccess(gameType: GameType, _) =>
          Ok(Json.toJson(Await.result(GameTypesDao.addGameType(gameType), 5.seconds))).as("application/json")
        case JsError(e) =>
          Logger.error("Invalid Post Body for addGameType: " + e)
          BadRequest(Json.toJson(GenericResponse(success = false, None, Some(e.map(_.toString()))))).as("application/json)")
      }
    }
  }

  def deleteGameType(id: Int) = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.DELETE)) {
    Action {
      Ok(Json.toJson(Await.result(GameTypesDao.deleteGameType(id), 5.seconds) match {
        case 1 => GenericResponse(success = true)
        case 0 => GenericResponse(success = false, None, Some(Seq(s"Game Type with id '$id' does not exist")))
      })).as("application/json")
    }
  }
}
