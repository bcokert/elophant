package object exception {
  class AppNotFoundException(msg: String, cause: Throwable) extends Exception(msg, cause)
  class PlayerNotFoundException(msg: String, cause: Throwable) extends Exception(msg, cause)
  class GameTypeNotFoundException(msg: String, cause: Throwable) extends Exception(msg, cause)

  class UnknownPSQLException(msg: String, cause: Throwable) extends Exception(msg, cause)
}
