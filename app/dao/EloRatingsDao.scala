package dao

import models.EloRating
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future

class EloRatingsDao(tag: Tag) extends Table[EloRating](tag, "rating") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def eloRating = column[Int]("elo_rating")
  def playerId = column[Int]("player_id")
  def leagueId = column[Int]("league_id")
  def gameTypeId = column[Int]("game_type_id")
  def * = (id, eloRating, playerId, leagueId, gameTypeId) <> ((arr: (Int, Int, Int, Int, Int)) => EloRating(arr._1, arr._2, arr._3, arr._4, arr._5), EloRating.unapply)
}

object EloRatingsDao extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  def getRating(playerId: Int, leagueId: Int, gameTypeId: Int): Future[EloRating] = queryOne(TableQuery[EloRatingsDao].filter(
    r => r.playerId === playerId && r.leagueId === leagueId && r.gameTypeId === gameTypeId)
  )
  def getRatingsByPlayerId(playerId: Int): Future[Seq[EloRating]] = query(TableQuery[EloRatingsDao].filter(_.playerId === playerId))
  def getRatingsByLeagueId(leagueId: Int): Future[Seq[EloRating]] = query(TableQuery[EloRatingsDao].filter(_.leagueId === leagueId))
  def getRatingsByGameTypeId(gameTypeId: Int): Future[Seq[EloRating]] = query(TableQuery[EloRatingsDao].filter(_.gameTypeId === gameTypeId))
  def getRatings: Future[Seq[EloRating]] = query(TableQuery[EloRatingsDao])

  def updateEloRating(eloRating: EloRating): Future[Int] = upsert[EloRating, EloRatingsDao](TableQuery[EloRatingsDao])(eloRating)
}
