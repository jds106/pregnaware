package database

import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._
import utils.AppConfig

/** Wraps up a database connection */
object ConnectionManager {

  private val url         = AppConfig.getDatabaseUrl()
  private val user        = AppConfig.getDatabaseUser()
  private val password    = AppConfig.getDatabasePassword()

  /** Creates a database connection (loan pattern) */
  def connection[T](f: MySQLDriver.backend.DatabaseDef => T) : T = {

    val db = Database.forURL(
      url      = ConnectionManager.url,
      driver   = "com.mysql.jdbc.Driver",
      user     = ConnectionManager.user,
      password = ConnectionManager.password)

    try {
      f(db)
    } finally {
      db.close()
    }
  }
}
