package integration

import _root_.util.TestUtils
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Result
import play.api.test._
import play.api.test.Helpers._
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import scala.concurrent.duration._
import org.scalatestplus.play._
import scala.concurrent.{Await, Future}

trait BaseIntegrationTest extends FlatSpec with Matchers with BeforeAndAfterEach with TestUtils with OneAppPerSuite {

  implicit override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map())

  /**
   * A function that should be overridden to reset all tables to their initial values.
   * Make sure it blocks until it's done!
   * It can assume the tables and databases (anything in an evolution) has already been done.
   * It should aggressively refresh all data to reduce false positives (trash everything, then re-init)
   * It should NOT assume other tests have not modified its data (reset everything it needs)
   */
  def resetDatabase(): Unit

  override def beforeEach(): Unit = resetDatabase()

  def waitFor[A](future: Future[A]): A = Await.result(future, 5.seconds)

  /**
   * Executes a fake request to the current test application. Should be called within withDatabase.
   * If executing a request that will result in json, use testJsonRequest, which wraps the output nicely
   * @param method The HTTP method, from play.api.http.HttpVerbs
   * @param url The url to hit. Should be exact, eg: "/player/4"
   * @return Future[Result]
   */
  def testRequest(method: String, url: String): Future[Result] = route(FakeRequest(method, url)).get

  /**
   * Execute a test with an available test application. You can then use routes or anything below in its context
   * @param config Optional overriding config. Can add or overwrite configs found in application-test.conf
   * @param test The test function. Receives an Either, left being the success response (headers + json), and right being the failure response (headers + string)
   * @return Unit
   */
  def withDatabase[T](config: Map[String, _] = Map())(test: JdbcBackend#DatabaseDef => T): T = {
    test(DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current).db)
  }

  /**
   * Execute a test with an available test application. You can then use routes or anything below in its context
   * @param test The test function. Receives an Either, left being the success response (headers + json), and right being the failure response (headers + string)
   * @return Unit
   */
  def withDatabase[T](test: JdbcBackend#DatabaseDef => T): T = withDatabase()(test)
}
