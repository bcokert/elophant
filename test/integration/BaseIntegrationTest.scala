package integration

import _root_.util.TestUtils
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}
import play.api.Play
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test._
import play.api.test.Helpers._
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import types.{PermissionLevels, PermissionTypes, PermissionLevel, PermissionType}
import scala.concurrent.duration._
import org.scalatestplus.play._
import scala.concurrent.{Await, Future}
import language.implicitConversions
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.PostgresDriver.api._

trait BaseIntegrationTest extends FlatSpec with Matchers with BeforeAndAfterEach with TestUtils with OneAppPerSuite {

  implicit override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map())
  private val tokenChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrztuvwxyz1234567890_"

  val integrationTestAppToken: String = (1 to 50).map(i => tokenChars.charAt(Math.round(Math.random() * (tokenChars.length - 1)).toInt)).mkString("")
  val integrationTestAppId = (Math.random() * 10000000).toInt
  val defaultHeaders = Seq(("Auth-Token", integrationTestAppToken))

  // Allow testers to pass JsValue to functions that require Option[JsValue]
  implicit def jsValueToSomeJsValue(json: JsValue): Option[JsValue] = Some(json)

  /**
   * A function that should be overridden to reset all tables to their initial values.
   * Make sure it blocks until it's done!
   * It can assume the tables and databases (anything in an evolution) has already been done.
   * It should aggressively refresh all data to reduce false positives (trash everything, then re-init)
   * It should NOT assume other tests have not modified its data (reset everything it needs)
   */
  def resetDatabase(): Unit

  /**
   * A function that should be manually called to set the permissions for a specific test. Will be automatically cleaned
   * up at the end of the test.
   * @return Unit
   */
  def setAppPermissions(permissions: Map[PermissionType, PermissionLevel]): Unit = {
    Logger.info(s"Creating Integration test app with id $integrationTestAppId and token $integrationTestAppToken")
    withDatabase { db =>
      waitFor(Future.sequence(permissions.map {
        case (permType: PermissionType, permLevel: PermissionLevel) => db.run(sqlu"INSERT INTO permission(permission_level, permission_type, app_id) values('#${permLevel.name}', '#${permType.name}', #$integrationTestAppId);")
      }))
    }
  }

  override def beforeEach(): Unit = {
    Logger.info("Resetting database for test")
    withDatabase { db =>
      waitFor(db.run(sqlu"INSERT INTO app(id, name, owner_name, description, auth_token) values(#$integrationTestAppId, 'IntegrationTestApp', 'Base Integration Test', 'The default app for integration tests', '#$integrationTestAppToken');"))
    }
    resetDatabase()
  }

  override def afterEach(): Unit = {
    Logger.info(s"Cleaning up old test apps")
    withDatabase { db =>
      waitFor(db.run(sqlu"DELETE FROM permission WHERE app_id=#$integrationTestAppId;"))
      waitFor(db.run(sqlu"DELETE FROM app WHERE id=#$integrationTestAppId;"))
    }
  }

  def waitFor[A](future: Future[A]): A = Await.result(future, 5.seconds)

  /**
   * Executes a fake request to the current test application.
   * @param method The HTTP method, from play.api.http.HttpVerbs
   * @param url The url to hit. Should be exact, eg: "/player/4"
   * @return Future[Result]
   */
  def testRequest(method: String, url: String, headers: Seq[(String, String)] = defaultHeaders): Future[Result] = route(FakeRequest(method, url, FakeHeaders(headers), AnyContentAsEmpty)).get

  /**
   * Executes a fake request with json to the current test application. Should be called within withDatabase.
   * If executing a request that will result in json, use testJsonRequest, which wraps the output nicely
   * @param method The HTTP method, from play.api.http.HttpVerbs
   * @param url The url to hit. Should be exact, eg: "/player/4"
   * @return Future[Result]
   */
  def testRequestWithJson(method: String, url: String, json: JsValue, headers: Seq[(String, String)] = defaultHeaders): Future[Result] = route(FakeRequest(
    method,
    url,
    FakeHeaders(
      headers ++ Seq("Content-type"->"application/json")
    ),
    json
  )).get

  /**
   * Convenience method that executes a testRequest and verifies it has the correct http code and optionally response body
   * @param method The http method to pass to testRequest
   * @param url The url to pass to testRequest
   * @param expectedResponseCode The expected http code. Defaults to 200
   * @param maybeExpectedJson Optional. If present, verifies that the response matches the given JsValue
   * @return Option[JsValue] None if maybeExpectedJson was None, otherwise the resulting JsValue
   */
  def testRequestAndVerify(method: String, url: String, headers: Seq[(String, String)] = defaultHeaders, expectedResponseCode: Int = OK)(maybeExpectedJson: Option[JsValue] = None): Option[JsValue] = {
    val response = testRequest(method, url, headers)
    status(response) should equal(expectedResponseCode)
    maybeExpectedJson map { expected =>
      val result = contentAsJson(response)
      result should equal(expected)
      result
    }
  }

  /**
   * Convenience method that executes a testRequest and verifies it has the correct http code and optionally response body
   * The request must return a json value
   * @param method The http method to pass to testRequest
   * @param url The url to pass to testRequest
   * @param expectedResponseCode The expected http code. Defaults to 200
   * @param testFunction A function that receives the result of the request and veirfies manually that it is correct
   * @return JsValue The resulting JsValue
   */
  def testRequestAndManuallyVerify(method: String, url: String, headers: Seq[(String, String)] = defaultHeaders, expectedResponseCode: Int = OK)(testFunction: JsValue => Unit): Option[JsValue] = {
    val response = testRequest(method, url, headers)
    status(response) should equal(expectedResponseCode)
    val result = contentAsJson(response)
    testFunction(result)
    result
  }

  /**
   * Convenience method that executes a testRequestWithJson and verifies it has the correct http code and optionally response body
   * @param method The http method to pass to testRequest
   * @param url The url to pass to testRequest
   * @param expectedResponseCode The expected http code. Defaults to 200
   * @param maybeExpectedJson Optional. If present, verifies that the response matches the given JsValue
   * @return Option[JsValue] None if maybeExpectedJson was None, otherwise the resulting JsValue
   */
  def testRequestWithJsonAndVerify(method: String, url: String, json: JsValue, headers: Seq[(String, String)] = defaultHeaders, expectedResponseCode: Int = OK)(maybeExpectedJson: Option[JsValue] = None): Option[JsValue] = {
    val response = testRequestWithJson(method, url, json, headers)
    status(response) should equal(expectedResponseCode)
    maybeExpectedJson map { expected =>
      val result = contentAsJson(response)
      result should equal(expected)
      result
    }
  }

  /**
   * Convenience method that executes a testRequestWithJson and verifies it has the correct http code and optionally response body
   * The request must return a json value
   * @param method The http method to pass to testRequest
   * @param url The url to pass to testRequest
   * @param expectedResponseCode The expected http code. Defaults to 200
   * @param testFunction A function that receives the result of the request and veirfies manually that it is correct
   * @return JsValue The resulting JsValue
   */
  def testRequestWithJsonAndManuallyVerify(method: String, url: String, json: JsValue, headers: Seq[(String, String)] = defaultHeaders, expectedResponseCode: Int = OK)(testFunction: JsValue => Unit): JsValue = {
    val response = testRequestWithJson(method, url, json, headers)
    status(response) should equal(expectedResponseCode)
    val result = contentAsJson(response)
    testFunction(result)
    result
  }

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
