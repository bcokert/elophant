package controllers

import dao.GameTypesDao
import dto.response.GenericResponse
import models.GameType
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import types.{PermissionLevels, PermissionTypes}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class GameTypeController extends BaseController with AccessControl {

  def getGameType(id: Int) = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ)) {
    Action { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      ResultWithJson(GameTypesDao.getGameType(id), Ok)
    }
  }

  def getGameTypes = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.READ)) {
    Action { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      ResultWithJson(GameTypesDao.getGameTypes, Ok)
    }
  }

  def addGameType() = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.CREATE)) {
    Action(parse.json) { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      Json.fromJson[GameType](request.body) match {
        case JsSuccess(gameType: GameType, _) =>
          ResultWithJson(GameTypesDao.addGameType(gameType), Ok)
        case JsError(e) =>
          ErrorWithJson(GenericResponse.fromJsonErrors(e), BadRequest, "Invalid Post Body for addGameType")
      }
    }
  }

  def deleteGameType(id: Int) = AccessControlledAction(Map(PermissionTypes.GAME_TYPE -> PermissionLevels.DELETE)) {
    Action { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      val response = GameTypesDao.deleteGameType(id).map {
        case 1 => GenericResponse(success = true)
        case 0 => GenericResponse(success = false, None, Some(Seq(s"Game Type with id '$id' does not exist")))
      }
      ResultWithJson(response, Ok)
    }
  }
}
