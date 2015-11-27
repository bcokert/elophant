package models

trait DatabaseModel {
  def copyWithId(id: Int): DatabaseModel
}
