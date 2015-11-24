package dao

import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import types.{PermissionLevels, PermissionLevel, PermissionType, PermissionTypes}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

case class Permission(id: Int, permissionLevel: PermissionLevel, permissionType: PermissionType, appId: Int)
case class PermissionTypeModel(permissionType: PermissionType, description: String)

class PermissionTypeDao(tag: Tag) extends Table[PermissionTypeModel](tag, "permission_type") {
  implicit val permissionTypeColumnMapper = MappedColumnType.base[PermissionType, String](
    _.name,
    PermissionTypes.fromName
  )

  def permissionType = column[PermissionType]("type")
  def description = column[String]("description")
  def * = (permissionType, description) <> (PermissionTypeModel.tupled, PermissionTypeModel.unapply)
}

class PermissionsDao(tag: Tag) extends Table[Permission](tag, "permission") {
  implicit val permissionLevelColumnMapper = MappedColumnType.base[PermissionLevel, String](
    _.name,
    PermissionLevels.fromName
  )
  implicit val permissionTypeColumnMapper = MappedColumnType.base[PermissionType, String](
    _.name,
    PermissionTypes.fromName
  )

  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def permissionLevel = column[PermissionLevel]("permission_level")
  def permissionType = column[PermissionType]("permission_type")
  def appId = column[Int]("app_id")
  def * = (id, permissionLevel, permissionType, appId) <> (
    (arr: (Int, PermissionLevel, PermissionType, Int)) => Permission(arr._1, arr._2, arr._3, arr._4),
    (p: Permission) => Some((p.id, p.permissionLevel, p.permissionType, p.appId))
    )
}

object PermissionsDao extends BaseDao {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("default")(Play.current)
  implicit val db = dbConfig.db

  implicit val permissionTypeColumnMapper = MappedColumnType.base[PermissionType, String](
    _.name,
    PermissionTypes.fromName
  )

  def getPermission(permissionType: PermissionType, appId: Int): Future[PermissionLevel] = queryOne(TableQuery[PermissionsDao].filter(
    perm => (perm.permissionType === permissionType) && (perm.appId === appId)
  )).map(_.permissionLevel)

  def getPermissions(permissionTypes: Iterable[PermissionType], appId: Int): Future[Map[PermissionType, PermissionLevel]] = {
    val theirPermissions = TableQuery[PermissionsDao]
    val allPermissionTypes = TableQuery[PermissionTypeDao]
    val query = ((theirPermissions join allPermissionTypes) on (_.permissionType === _.permissionType)).filter {
      case (permDao: PermissionsDao, permTypeDao: PermissionTypeDao) => permDao.appId === appId
    }

    val id = Math.abs(Random.nextInt())
    Logger.info(s"Executing SQL <$id>: ${query.result.statements.head}")
    val result = db.run(query.result).recover {
      case (e: Throwable) =>
        Logger.error(s"SQL join action error: \n$e")
        throw e
    }.map {
      case res =>
        Logger.info(s"Result of SQL <$id>: ${res.mkString(",")}")
        res
    }

    result.map { joined =>
      joined.map {
        case (permission: Permission, permissionTypeModel: PermissionTypeModel) => (permissionTypeModel.permissionType, permission.permissionLevel)
      }.toMap
    }
  }
}
