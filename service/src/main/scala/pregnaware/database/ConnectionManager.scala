package pregnaware.database

import com.typesafe.scalalogging.StrictLogging
import pregnaware.utils.AppConfig
import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/** Wraps up a database connection */
object ConnectionManager extends StrictLogging {

  private val url = AppConfig.getDatabaseUrl
  private val user = AppConfig.getDatabaseUser
  private val password = AppConfig.getDatabasePassword

  /** Creates a database connection */
  def connection[T](f: MySQLDriver.backend.DatabaseDef => Future[T])(implicit executor : ExecutionContext): Future[T] = {
    connection("N/A")(f)
  }

  /** Creates a database connection */
  def connection[T](label: String)(f: MySQLDriver.backend.DatabaseDef => Future[T])(implicit executor : ExecutionContext): Future[T] = {
    val startTime = System.currentTimeMillis()

    val db = Database.forURL(
      url = ConnectionManager.url,
      driver = "com.mysql.jdbc.Driver",
      user = ConnectionManager.user,
      password = ConnectionManager.password)

    // Schedule the task to run
    logger.info(f"[$label] Starting query...")
    val future = f(db)

    // Once complete, close the database connection
    future.onComplete {
      case t =>
        val runTime = (System.currentTimeMillis() - startTime) / 1000.0
        logger.info(f"[$label] Query took $runTime%.2fs")
        db.close()
    }

    // Return this future
    future
  }
}
