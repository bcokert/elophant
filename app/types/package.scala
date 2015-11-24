import slick.driver.PostgresDriver.api._

package object types {
  type Score = Double
  type Rating = Int

  sealed abstract case class PermissionLevel(order: Int, name: String) extends Ordered[PermissionLevel] {
    def compare(that: PermissionLevel) = this.order - that.order
    def equals(that: PermissionLevel) = this.order == that.order
    override def toString = this.name
  }

  object PermissionLevels {
    object NONE extends PermissionLevel(0, "NONE")
    object READ extends PermissionLevel(1, "READ")
    object CREATE extends PermissionLevel(2, "CREATE")
    object UPDATE extends PermissionLevel(3, "UPDATE")
    object DELETE extends PermissionLevel(4, "DELETE")

    val all = Seq(NONE, READ, CREATE, UPDATE, DELETE)
    def fromName(name: String): PermissionLevel = name match {
      case "NONE" => PermissionLevels.NONE
      case "READ" => PermissionLevels.READ
      case "CREATE" => PermissionLevels.CREATE
      case "UPDATE" => PermissionLevels.UPDATE
      case "DELETE" => PermissionLevels.DELETE
    }
  }

  sealed abstract case class PermissionType(name: String) {
    override def toString = this.name
  }

  object PermissionTypes {
    object PLAYER extends PermissionType("PLAYER")
    object GAME_TYPE extends PermissionType("GAME_TYPE")
    object RATING extends PermissionType("RATING")

    val all = Seq(PLAYER, GAME_TYPE, RATING)
    def fromName(name: String): PermissionType = name match {
      case "PLAYER" => PermissionTypes.PLAYER
      case "GAME_TYPE" => PermissionTypes.GAME_TYPE
      case "RATING" => PermissionTypes.RATING
    }
  }
}
