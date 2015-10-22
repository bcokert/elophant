package controllers

import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import play.api.Logger
import slick.dbio.DBIO
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def index = Action {
//    testdb()
    Ok(views.html.index("Your new application is ready."))
  }

  def testdb(): Unit = {
    val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
    val db = dbConfig.db

    class TestUsers(tag: Tag) extends Table[(Int, String, String)](tag, "test_user") {
      def id = column[Int]("id", O.PrimaryKey)
      def email = column[String]("email")
      def name = column[String]("name")
      def * = (id, email, name)
    }

    val testUsers = TableQuery[TestUsers]

    val setup = DBIO.seq(
      testUsers.schema.create,
      testUsers +=(123, "bob@hotmail.com", "Bob")
    )

    //    val setupFixture = db.run(setup)

    val result = db.run(testUsers.result)
    result.onSuccess {
      case s =>
        println("Users:")
        s.foreach {
          case (id, email, name) => println(id + ": " + name + ", " + email)
        }
    }

    result.onFailure {
      case e => println(e)
    }
  }
}
