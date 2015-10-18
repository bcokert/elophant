package controllers

import play.api._
import play.api.mvc._
import play.api.Logger
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def index = Action {
    testdb()
    Ok(views.html.index("Your new application is ready."))
  }

  def testdb(): Unit = {
    val db = sys.env.getOrElse("ELOPHANT_ENV", "local") match {
      case "local" => Database.forConfig("db.local")
      case "dev" => Database.forConfig("db.dev")
    }

    case class TestUser(id: Int, name: String)

    class TestUsers(tag: Tag) extends Table[(Int, String)](tag, "TESTUSERS") {
      def id = column[Int]("ID", O.PrimaryKey)
      def name = column[String]("NAME")
      def * = (id, name)
    }

    val testUsers = TableQuery[TestUsers]

//    val setup = DBIO.seq(
//      testUsers.schema.create,
//
//      testUsers += (123, "Bob"),
//      testUsers += (456, "Jim"),
//      testUsers += (789, "Kerry")
//    )
//
//    val setupFixture = db.run(setup)

    println("Users:")
    db.run(testUsers.result) map (_.foreach {
      case (id, name) => println("  " + name + "(" + id + ")")
    })
  }
}
