package models

import play.api.libs.json._

case class GameResult(player1Id: Int, player2Id: Int, didPlayer1Win: Boolean, gameTypeId: Int)

object GameResult {
  implicit val gameResultReads = Json.reads[GameResult]
  implicit val gameResultWrites = Json.writes[GameResult]
}
