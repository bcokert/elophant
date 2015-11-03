package dao

import play.api.Logger
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend
import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

trait BaseDao {

  private def sqlActionWithLogging[A, C <: Effect, E, D <: Table[E]](action: DBIOAction[A, NoStream, C], query: Query[D, E, Seq])(toSqlFn: Query[D, E, Seq] => String, loggingFn: A => String)
    (implicit db: JdbcBackend#DatabaseDef): Future[A] = {
    val id = Random.nextInt()
    Logger.info("Executing SQL <" + id + ">: " + toSqlFn(query))
    db.run(action).recover {
      case (e: Throwable) =>
        Logger.error(e.getMessage)
        throw e
    }.map {
      case res =>
        Logger.info("Result of SQL <" + id + ">: " + loggingFn(res))
        res
    }
  }

  def query[B, A <: Table[B]](query: Query[A, B, Seq])(implicit db: JdbcBackend#DatabaseDef): Future[Seq[B]] = {
    sqlActionWithLogging(query.result, query)(_.result.statements.head, _.mkString(","))
  }

  def queryOne[B, A <: Table[B]](query: Query[A, B , Seq])(implicit db: JdbcBackend#DatabaseDef): Future[B] = {
    sqlActionWithLogging(query.result.head, query)(_.result.statements.head, _.toString)
  }

  def delete[B, A <: Table[B]](query: Query[A, B, Seq])(implicit db: JdbcBackend#DatabaseDef): Future[Int] = {
    sqlActionWithLogging(query.delete, query)(_.delete.statements.head, _.toString)
  }

  def insert[B, A <: Table[B]](query: TableQuery[A])(row: B)(implicit db: JdbcBackend#DatabaseDef): Future[Unit] = {
    sqlActionWithLogging(DBIO.seq(query += row), query)(_.insertStatement, _ => "success")
  }
}
