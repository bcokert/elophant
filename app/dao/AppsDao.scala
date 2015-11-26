package dao

import exception.AppNotFoundException
import models.Player
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class App(id: Int, name: String, ownerName: String, description: String, authToken: String)

class AppsDao(tag: Tag) extends Table[App](tag, "app") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def ownerName = column[String]("owner_name")
  def description = column[String]("description")
  def authToken = column[String]("auth_token")
  def * = (id, name, ownerName, description, authToken) <>(App.tupled, App.unapply)
}

object AppsDao extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  def getAppByToken(token: String): Future[App] = queryOne(TableQuery[AppsDao].filter(_.authToken === token)).recoverWith {
    case (e: NoSuchElementException) =>
      Logger.info("Attempted to search for an app with a token that doesn't exist")
      throw new AppNotFoundException(s"An app with the given token '$token' was not found")
  }
}
