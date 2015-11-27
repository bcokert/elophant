package dao

import models.Player
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future

class PlayersDao(tag: Tag) extends Table[Player](tag, "player") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def first_name = column[String]("first_name")
  def last_name = column[String]("last_name")
  def email = column[String]("email")
  def * = (id, first_name, last_name, email) <> ((arr: (Int, String, String, String)) => Player(arr._1, arr._2, arr._3, arr._4), Player.unapply)
}

object PlayersDao extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  def getPlayer(id: Int): Future[Player] = queryOne(TableQuery[PlayersDao].filter(_.id === id))
  def getPlayers: Future[Seq[Player]] = query(TableQuery[PlayersDao])
  def addPlayer(player: Player): Future[Player] = insertAndReturnResult[Player, PlayersDao](TableQuery[PlayersDao])(player, _.id)
  def deletePlayer(id: Int): Future[Int] = delete[Player, PlayersDao](TableQuery[PlayersDao].filter(_.id === id))
}
