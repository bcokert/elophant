package models

import play.api.libs.json._
import types.Score

case class GameResult(player1Id: Int, player2Id: Int, score: Score, gameTypeId: Int)

object GameResult {
  implicit val gameResultReads = Json.reads[GameResult]
  implicit val gameResultWrites = Json.writes[GameResult]
}
