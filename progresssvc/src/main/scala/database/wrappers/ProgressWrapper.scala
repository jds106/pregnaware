package database.wrappers

import java.time.LocalDate
import java.sql.Date

import database.ConnectionManager._
import database.schema.Tables._
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/* Due date functions */
trait ProgressWrapper {

  implicit def executionContext: ExecutionContext

  /** Gets an existing due date */
  def getDueDate(userId: Int) : Future[Option[ProgressRow]] = {
    connection { db =>
      db.run(Progress.filter(_.userid === userId).result.headOption)
    }
  }

  /** Sets a due date (either adding a due date, or replacing an existing one) */
  def setDueDate(userId: Int, dueDate: LocalDate) : Future[ProgressRow] = {
    connection { db =>
      getDueDate(userId).flatMap {
        case Some(progressRow) =>
          Future.successful(progressRow)

        case None =>
          val insertQuery = Progress returning Progress
          val action = insertQuery += ProgressRow(userId, Date.valueOf(dueDate))
          db.run(action)
      }
    }
  }

  /** Removes a due date */
  def deleteDueDate(userId: Int) : Future[Boolean] = {
    connection { db =>
      db.run(Progress.filter(_.userid === userId).delete).map {
        case 0 => false
        case 1 => true
        case _ => throw new Exception(s"Removed multiple due dates for user: $userId")
      }
    }
  }
}
