package dao

import models.GameType
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future

class GameTypesDao(tag: Tag) extends Table[GameType](tag, "game_type") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def * = (id, name, description) <> ((arr: (Int, String, String)) => GameType(arr._1, arr._2, arr._3), GameType.unapply)
}

object GameTypesDao extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  def getGameType(id: Int): Future[GameType] = queryOne(TableQuery[GameTypesDao].filter(_.id === id))
  def getGameTypes: Future[Seq[GameType]] = query(TableQuery[GameTypesDao])
  def addGameType(gameType: GameType): Future[GameType] = insertAndReturnResult[GameType, GameTypesDao](TableQuery[GameTypesDao])(gameType, _.id)
  def deleteGameType(id: Int): Future[Int] = delete[GameType, GameTypesDao](TableQuery[GameTypesDao].filter(_.id === id))
}
