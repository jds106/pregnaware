package pregnaware.database

import pregnaware.utils.AppConfig
import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/** Wraps up a database connection */
object ConnectionManager {

  private val url = AppConfig.getDatabaseUrl
  private val user = AppConfig.getDatabaseUser
  private val password = AppConfig.getDatabasePassword

  /** Creates a database connection (loan pattern) */
  def connection[T](f: MySQLDriver.backend.DatabaseDef => Future[T])(implicit executor : ExecutionContext): Future[T] = {

    val db = Database.forURL(
      url = ConnectionManager.url,
      driver = "com.mysql.jdbc.Driver",
      user = ConnectionManager.user,
      password = ConnectionManager.password)

    // Schedule the task to run
    val future = f(db)

    // Once complete, close the database connection
    future.onComplete {
      case t => db.close()
    }

    // Return this future
    future
  }
}
