package controllers

import dto.response.GenericSuccessResponse
import models.Player
import play.api.mvc._
import play.api.Logger
import play.api.libs.json._
import dao.PlayersDao
import error.jsonErrorWrites
import types.{PermissionLevels, PermissionTypes}

import scala.concurrent.Await
import scala.concurrent.duration._

class PlayerController extends Controller with AccessControl {

  def getPlayer(id: Int) = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.READ)) {
    Action {
      Ok(Json.toJson(Await.result(PlayersDao.getPlayer(id), 5.seconds))).as("application/json")
    }
  }

  def getPlayers = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.READ)) {
    Action {
      Ok(Json.toJson(Await.result(PlayersDao.getPlayers, 5.seconds).map(Json.toJson(_)))).as("application/json")
    }
  }

  def addPlayer() = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.CREATE)) {
    Action(parse.json) { request =>
      Json.fromJson[Player](request.body) match {
        case JsSuccess(p: Player, _) =>
          Await.result(PlayersDao.addPlayer(p), 5.seconds)
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
  }

  def deletePlayer(id: Int) = AccessControlledAction(Map(PermissionTypes.PLAYER -> PermissionLevels.DELETE)) {
    Action {
      Ok(Json.toJson(Await.result(PlayersDao.deletePlayer(id), 5.seconds) match {
        case 1 => GenericSuccessResponse(true)
        case 0 => GenericSuccessResponse(false)
      })).as("application/json")
    }
  }
}
