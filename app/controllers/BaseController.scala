package controllers

import play.api.Logger
import play.api.libs.json.{Writes, Json}
import play.api.mvc._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

trait BaseController extends Controller {
  def ResultWithJson[A](future: Future[A], method: Status)(implicit writes: Writes[A]) = {
    val result = Json.toJson(Await.result(future, 5.seconds))
    Logger.info(s"RESPONSE: ${result.toString()}")
    method(result).as("application/json")
  }

  def ResultWithJson[A](output: A, method: Status)(implicit writes: Writes[A]) = {
    val result = Json.toJson(output)
    Logger.info(s"RESPONSE: ${result.toString()}")
    method(result).as("application/json")
  }

  def ErrorWithJson[A](future: Future[A], method: Status, message: String)(implicit writes: Writes[A]) = {
    val result = Json.toJson(Await.result(future, 5.seconds))
    Logger.error(s"RESPONSE ERROR: $message ${result.toString()}")
    method(result).as("application/json")
  }

  def ErrorWithJson[A](output: A, method: Status, message: String)(implicit writes: Writes[A]) = {
    val result = Json.toJson(output)
    Logger.error(s"RESPONSE ERROR: $message ${result.toString()}")
    method(result).as("application/json")
  }
}
