package dao

import models.Player
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Players(tag: Tag) extends Table[Player](tag, "player") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def first_name = column[String]("first_name")
  def last_name = column[String]("last_name")
  def email = column[String]("email")
  def * = (id, first_name, last_name, email) <> ((arr: (Int, String, String, String)) => Player(arr._1, arr._2, arr._3, arr._4), Player.unapply)
}

object Players extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  def getPlayer(id: Int): Future[Player] = db.run(TableQuery[Players].filter(_.id === id).result.head).recover {
    case (e: Throwable) =>
      Logger.error(e.getMessage)
      throw e
  }

  def getPlayers: Future[Seq[Player]] = queryAll(TableQuery[Players])
  def addPlayer(player: Player): Future[Unit] = insert(TableQuery[Players])(player)
}
