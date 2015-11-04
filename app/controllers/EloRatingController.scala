package controllers

import _root_.util.StandardEloCalculator
import dao.EloRatingsDao
import error.jsonErrorWrites
import models.{GameResult, GenericIsSuccess, EloRating}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class EloRatingController extends Controller {

  def getEloRatings(maybePlayerId: Option[Int], maybeLeagueId: Option[Int], maybeGameTypeId: Option[Int]) = Action {
    val badRequestJson = Json.obj(
      "success" -> false,
      "reason" -> "Not yet supported. Please provide 0, 1, or 3 query params"
    )

    (maybePlayerId, maybeLeagueId, maybeGameTypeId) match {
      case (Some(playerId), Some(leagueId), Some(gameTypeId)) => Ok(Json.toJson(Await.result(EloRatingsDao.getRating(playerId, leagueId, gameTypeId), 5.seconds))).as("application/json")
      case (Some(playerId), Some(leagueId), None) => BadRequest(badRequestJson).as("application/json")
      case (Some(playerId), None, Some(gameTypeId)) => BadRequest(badRequestJson).as("application/json")
      case (None, Some(leagueId), Some(gameTypeId)) => BadRequest(badRequestJson).as("application/json")
      case (Some(playerId), None, None) => Ok(Json.toJson(Await.result(EloRatingsDao.getRatingsByPlayerId(playerId), 5.seconds).map(Json.toJson(_)))).as("application/json")
      case (None, Some(leagueId), None) => Ok(Json.toJson(Await.result(EloRatingsDao.getRatingsByLeagueId(leagueId), 5.seconds).map(Json.toJson(_)))).as("application/json")
      case (None, None, Some(gameTypeId)) => Ok(Json.toJson(Await.result(EloRatingsDao.getRatingsByGameTypeId(gameTypeId), 5.seconds).map(Json.toJson(_)))).as("application/json")
      case (None, None, None) => Ok(Json.toJson(Await.result(EloRatingsDao.getRatings, 5.seconds).map(Json.toJson(_)))).as("application/json")
    }
  }

  def addGameResult() = Action(parse.json) { request =>
    Json.fromJson[GameResult](request.body) match {
      case JsSuccess(g: GameResult, _) =>
        val (player1Id, player2Id, score, leagueId, gameTypeId) = (
          g.player1Id,
          g.player2Id,
          if (g.didPlayer1Win) 1 else 0,
          g.leagueId,
          g.gameTypeId)

        val futureRating1 = EloRatingsDao.getRating(player1Id, leagueId, gameTypeId).map(r => (r.id, r.rating)).recover {
          case e => Logger.info(s"No rating found for player $player1Id and league $leagueId and game type $gameTypeId. Setting to 1000 by default"); (0, 1000)
        }
        val futureRating2 = EloRatingsDao.getRating(player2Id, leagueId, gameTypeId).map(r => (r.id, r.rating)).recover {
          case e => Logger.info(s"No rating found for player $player2Id and league $leagueId and game type $gameTypeId. Setting to 1000 by default"); (0, 1000)
        }

        val updates = for {
          (eloid1, rating1) <- futureRating1
          (eloid2, rating2) <- futureRating2
          deltaRating = StandardEloCalculator.getDeltaRating(rating1, rating2, score)
          elo1WasUpdated <- EloRatingsDao.updateEloRating(EloRating(eloid1, rating1 + deltaRating, player1Id, leagueId, gameTypeId))
          elo2WasUpdated <- EloRatingsDao.updateEloRating(EloRating(eloid2, rating2 - deltaRating, player2Id, leagueId, gameTypeId))
        } yield (elo1WasUpdated, elo2WasUpdated)

        Await.result(updates, 5.seconds) match {
          case (1, 1) => Ok(Json.toJson(GenericIsSuccess(true))).as("application/json)")
          case (0, 1) => BadRequest(Json.obj(
            "success" -> JsBoolean(false),
            "reason" -> s"Player $player1Id's elo did not update correctly"
          )).as("application/json)")
          case (1, 0) => BadRequest(Json.obj(
            "success" -> JsBoolean(false),
            "reason" -> s"Player $player2Id's elo did not update correctly"
          )).as("application/json)")
          case (0, 0) => BadRequest(Json.obj(
            "success" -> JsBoolean(false),
            "reason" -> s"Neither Players elo updated correctly"
          )).as("application/json)")
        }
      case err@JsError(_) =>
        Logger.error("Invalid Post Body for addGameResult: " + Json.toJson(err))
        BadRequest(Json.obj(
          "success" -> JsBoolean(false),
          "reason" -> "Invalid Post Body",
          "errors" -> Json.toJson(err)
        )).as("application/json)")
    }
  }
}
