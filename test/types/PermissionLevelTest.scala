package types

import org.scalatest.{FlatSpec, Matchers}

class PermissionLevelTest extends FlatSpec with Matchers {
  "PermissionLevels" should "be comparable by equality" in {
    PermissionLevels.NONE should equal(PermissionLevels.NONE)
    PermissionLevels.READ should equal(PermissionLevels.READ)
    PermissionLevels.CREATE should equal(PermissionLevels.CREATE)
    PermissionLevels.UPDATE should equal(PermissionLevels.UPDATE)
    PermissionLevels.DELETE should equal(PermissionLevels.DELETE)
  }

  it should "be comparable by greater and less than" in {
    PermissionLevels.NONE < PermissionLevels.READ should equal(true)
    PermissionLevels.READ < PermissionLevels.CREATE should equal(true)
    PermissionLevels.CREATE < PermissionLevels.UPDATE should equal(true)
    PermissionLevels.UPDATE < PermissionLevels.DELETE should equal(true)
    PermissionLevels.READ < PermissionLevels.DELETE should equal(true)
    PermissionLevels.CREATE < PermissionLevels.UPDATE should equal(true)
  }

  it should "support filtering by largest permission type" in {
    Seq(PermissionLevels.READ, PermissionLevels.UPDATE, PermissionLevels.CREATE).max should equal(PermissionLevels.UPDATE)
    Seq(PermissionLevels.READ, PermissionLevels.UPDATE, PermissionLevels.CREATE).min should equal(PermissionLevels.READ)
  }

  it should "be creatable from name only" in {
    PermissionLevels.fromName("NONE") should equal(PermissionLevels.NONE)
    PermissionLevels.fromName("READ") should equal(PermissionLevels.READ)
    PermissionLevels.fromName("CREATE") should equal(PermissionLevels.CREATE)
    PermissionLevels.fromName("UPDATE") should equal(PermissionLevels.UPDATE)
    PermissionLevels.fromName("DELETE") should equal(PermissionLevels.DELETE)
  }
}
