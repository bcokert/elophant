package dao

import models.League
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future

class LeaguesDao(tag: Tag) extends Table[League](tag, "league") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def * = (id, name) <> ((arr: (Int, String)) => League(arr._1, arr._2), League.unapply)
}

object LeaguesDao extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  def getLeague(id: Int): Future[League] = queryOne(TableQuery[LeaguesDao].filter(_.id === id))
  def getLeagues: Future[Seq[League]] = query(TableQuery[LeaguesDao])
  def addLeague(league: League): Future[Unit] = insert[League, LeaguesDao](TableQuery[LeaguesDao])(league)
  def deleteLeague(id: Int): Future[Int] = delete[League, LeaguesDao](TableQuery[LeaguesDao].filter(_.id === id))
}
