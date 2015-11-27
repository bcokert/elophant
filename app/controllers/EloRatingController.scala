package controllers

import _root_.util.StandardEloCalculator
import dao.EloRatingsDao
import dto.response.GenericResponse
import exception.{GameTypeNotFoundException, PlayerNotFoundException, UnknownPSQLException}
import models.{GameResult, EloRating}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import types.{PermissionLevels, PermissionTypes}

import scala.concurrent.{Future, Await}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

class EloRatingController extends Controller with AccessControl {

  def getEloRatings(maybePlayerId: Option[Int], maybeGameTypeId: Option[Int]) = AccessControlledAction(Map(PermissionTypes.RATING -> PermissionLevels.READ)) {
    Action {
      (maybePlayerId, maybeGameTypeId) match {
        case (Some(playerId), Some(gameTypeId)) => Ok(Json.toJson(Await.result(EloRatingsDao.getRating(playerId, gameTypeId), 5.seconds))).as("application/json")
        case (Some(playerId), None) => Ok(Json.toJson(Await.result(EloRatingsDao.getRatingsByPlayerId(playerId), 5.seconds))).as("application/json")
        case (None, Some(gameTypeId)) => Ok(Json.toJson(Await.result(EloRatingsDao.getRatingsByGameTypeId(gameTypeId), 5.seconds))).as("application/json")
        case (None, None) => Ok(Json.toJson(Await.result(EloRatingsDao.getRatings, 5.seconds))).as("application/json")
      }
    }
  }

  def addGameResult() = AccessControlledAction(Map(PermissionTypes.RATING -> PermissionLevels.UPDATE)) {
    Action(parse.json) { request =>
      Json.fromJson[GameResult](request.body) match {
        case JsSuccess(g: GameResult, _) =>
          val (player1Id, player2Id, score, gameTypeId) = (
            g.player1Id,
            g.player2Id,
            g.score,
            g.gameTypeId)

          val futureRating1 = EloRatingsDao.getRating(player1Id, gameTypeId).map(r => (r.id, r.rating)).recover {
            case e => Logger.info(s"No rating found for player $player1Id and game type $gameTypeId. Setting to 1000 by default"); (0, 1000)
          }
          val futureRating2 = EloRatingsDao.getRating(player2Id, gameTypeId).map(r => (r.id, r.rating)).recover {
            case e => Logger.info(s"No rating found for player $player2Id and game type $gameTypeId. Setting to 1000 by default"); (0, 1000)
          }

          val updates = for {
            (eloid1, rating1) <- futureRating1
            (eloid2, rating2) <- futureRating2
            deltaRating = StandardEloCalculator.getDeltaRating(rating1, rating2, score)
            elo1WasUpdated <- EloRatingsDao.updateEloRating(EloRating(eloid1, rating1 + deltaRating, player1Id, gameTypeId)).recover {
              case e: PlayerNotFoundException => 0
              case e: GameTypeNotFoundException => 0
            }
            elo2WasUpdated <- EloRatingsDao.updateEloRating(EloRating(eloid2, rating2 - deltaRating, player2Id, gameTypeId)).recover {
              case e: PlayerNotFoundException => 0
              case e: GameTypeNotFoundException => 0
            }
          } yield (elo1WasUpdated, elo2WasUpdated)

          Await.result(updates, 5.seconds) match {
            case (1, 1) => Ok(Json.toJson(GenericResponse(success = true))).as("application/json)")
            case (0, 1) => BadRequest(Json.toJson(GenericResponse(success = false, None, Some(Seq(s"Rating for Player '$player1Id' and Game Type '$gameTypeId' does not exist"))))).as("application/json)")
            case (1, 0) => BadRequest(Json.toJson(GenericResponse(success = false, None, Some(Seq(s"Rating for Player '$player2Id' and Game Type '$gameTypeId' does not exist"))))).as("application/json)")
            case (0, 0) => BadRequest(Json.toJson(GenericResponse(success = false, None, Some(Seq(player1Id, player2Id).map(id => s"Rating for Player '$id' and Game Type '$gameTypeId' does not exist"))))).as("application/json)")
          }
        case JsError(e) =>
          Logger.error("Invalid Post Body for addGameResult: " + e)
          BadRequest(Json.toJson(GenericResponse(success = false, None, Some(e.map(_.toString()))))).as("application/json)")
      }
    }
  }
}
