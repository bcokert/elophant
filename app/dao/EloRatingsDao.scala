package dao

import exception.{UnknownPSQLException, GameTypeNotFoundException, PlayerNotFoundException}
import models.EloRating
import org.postgresql.util.PSQLException
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class EloRatingsDao(tag: Tag) extends Table[EloRating](tag, "rating") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def eloRating = column[Int]("elo_rating")
  def playerId = column[Int]("player_id")
  def gameTypeId = column[Int]("game_type_id")
  def * = (id, eloRating, playerId, gameTypeId) <> ((arr: (Int, Int, Int, Int)) => EloRating(arr._1, arr._2, arr._3, arr._4), EloRating.unapply)
}

object EloRatingsDao extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  def getRating(playerId: Int, gameTypeId: Int): Future[EloRating] = queryOne(TableQuery[EloRatingsDao].filter(
    r => r.playerId === playerId && r.gameTypeId === gameTypeId)
  )
  def getRatingsByPlayerId(playerId: Int): Future[Seq[EloRating]] = query(TableQuery[EloRatingsDao].filter(_.playerId === playerId))
  def getRatingsByGameTypeId(gameTypeId: Int): Future[Seq[EloRating]] = query(TableQuery[EloRatingsDao].filter(_.gameTypeId === gameTypeId))
  def getRatings: Future[Seq[EloRating]] = query(TableQuery[EloRatingsDao])

  def updateEloRating(eloRating: EloRating): Future[Int] = upsert[EloRating, EloRatingsDao](TableQuery[EloRatingsDao])(eloRating).recoverWith {
    case e: PSQLException if e.getMessage.contains(s"Detail: Key (game_type_id)=(${eloRating.gameTypeId}) is not present") =>
      Logger.warn(s"Attempted to update elo rating, but Game Type did not exist. Rating to update: $eloRating")
      throw new GameTypeNotFoundException(s"A Game Type with that ID was not found, while updating elo rating to new value $eloRating", e)
    case e: PSQLException if e.getMessage.contains(s"Detail: Key (player_id)=(${eloRating.playerId}) is not present") =>
      Logger.warn(s"Attempted to update elo rating, but Player did not exist. Rating to update: $eloRating")
      throw new PlayerNotFoundException(s"A Player with that ID was not found, while updating elo rating to new value $eloRating", e)
    case e: PSQLException =>
      Logger.warn(s"Attempted to update elo rating, an unknown issue occurred. Rating to update: $eloRating")
      throw new UnknownPSQLException(s"An unknown issue occurred, while updating elo rating to new value $eloRating", e)
  }
}
