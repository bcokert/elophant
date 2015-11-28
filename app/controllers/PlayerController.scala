package controllers

import dto.response.GenericResponse
import models.Player
import play.api.mvc._
import play.api.Logger
import play.api.libs.json._
import dao.PlayersDao
import types.{PermissionLevels, PermissionTypes}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class PlayerController extends BaseController with AccessControl {

  def getPlayer(id: Int) = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.READ)) {
    Action { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      ResultWithJson(PlayersDao.getPlayer(id), Ok)
    }
  }

  def getPlayers = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.READ)) {
    Action { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      ResultWithJson(PlayersDao.getPlayers, Ok)
    }
  }

  def addPlayer() = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.CREATE)) {
    Action(parse.json) { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      Json.fromJson[Player](request.body) match {
        case JsSuccess(p: Player, _) =>
          ResultWithJson(PlayersDao.addPlayer(p), Ok)
        case JsError(e) =>
          ErrorWithJson(GenericResponse.fromJsonErrors(e), BadRequest, "Invalid Post Body for addPlayer")
      }
    }
  }

  def deletePlayer(id: Int) = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.DELETE)) {
    Action { request =>
      Logger.info(s"REQUEST: ${request.method} ${request.path} ${request.body}")
      val response = PlayersDao.deletePlayer(id).map {
        case 1 => GenericResponse(success = true)
        case 0 => GenericResponse(success = false, None, Some(Seq(s"Player with id '$id' does not exist")))
      }
      ResultWithJson(response, Ok)
    }
  }
}
