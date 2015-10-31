package dao

import play.api.Logger
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend
import slick.lifted.AbstractTable
import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

trait BaseDao {
  def queryAll[A <: AbstractTable[_]](tableQuery: TableQuery[A])(implicit db: JdbcBackend#DatabaseDef) =
    db.run(tableQuery.result).recover {
      case (e: Throwable) =>
        Logger.error(e.getMessage)
        throw e
    }

  def insert[A <: AbstractTable[_], B <: A#TableElementType](tableQuery: TableQuery[A])(row: B)(implicit db: JdbcBackend#DatabaseDef) =
    db.run(DBIO.seq(tableQuery += row)).recover {
      case (e: Throwable) =>
        Logger.error(e.getMessage)
        throw e
    }
}
