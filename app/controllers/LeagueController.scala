package controllers

import dao.LeaguesDao
import error.jsonErrorWrites
import models.{League, GenericIsSuccess}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration._

class LeagueController extends Controller {

  def getLeague(id: Int) = Action {
    Ok(Json.toJson(Await.result(LeaguesDao.getLeague(id), 5.seconds))).as("application/json")
  }

  def getLeagues = Action {
    Ok(Json.toJson(Await.result(LeaguesDao.getLeagues, 5.seconds).map(Json.toJson(_)))).as("application/json")
  }

  def addLeague() = Action(parse.json) { request =>
    Json.fromJson[League](request.body) match {
      case JsSuccess(league: League, _) =>
        Await.result(LeaguesDao.addLeague(league), 5.seconds)
        Ok(Json.obj(
          "success" -> JsBoolean(true)
        )).as("application/json")
      case err@JsError(_) =>
        Logger.error("Invalid Post Body for addLeague: " + Json.toJson(err))
        BadRequest(Json.obj(
          "success" -> JsBoolean(false),
          "reason" -> "Invalid Post Body",
          "errors" -> Json.toJson(err)
        )).as("application/json)")
    }
  }

  def deleteLeague(id: Int) = Action {
    Ok(Json.toJson(Await.result(LeaguesDao.deleteLeague(id), 5.seconds) match {
      case 1 => GenericIsSuccess(true)
      case 0 => GenericIsSuccess(false)
    })).as("application/json")
}
}
