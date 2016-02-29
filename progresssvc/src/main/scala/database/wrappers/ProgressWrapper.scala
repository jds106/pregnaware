package database.wrappers

import java.time.LocalDate
import java.sql.Date

import database.ConnectionManager._
import database.schema.Tables._
import progress.ProgressPersistence
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/* Due date functions */
trait ProgressWrapper extends ProgressPersistence {

  implicit def executionContext: ExecutionContext

  /** Gets an existing due date */
  def getDueDate(userId: Int) : Future[Option[LocalDate]] = {
    connection { db =>
      db.run(Progress.filter(_.userid === userId).map(_.duedate).result.headOption).map {
        case None => None
        case Some(d) => Some(d.toLocalDate)
      }
    }
  }

  /** Sets a due date (either adding a due date, or replacing an existing one) */
  def setDueDate(userId: Int, dueDate: LocalDate) : Future[LocalDate] = {
    connection { db =>
      getDueDate(userId).flatMap {
        case Some(progressRow) =>
          val query = Progress.filter(_.userid === userId).map(_.duedate)
          val action = query.update(Date.valueOf(dueDate))
          db.run(action).map {
            case 1 => dueDate
            case n => throw new Exception(s"Modified $n due dates for user: $userId")
          }

        case None =>
          val action = Progress += ProgressRow(userId, Date.valueOf(dueDate))
          db.run(action).map {
            case 1 => dueDate
            case n => throw new Exception(s"Added $n due dates for user: $userId")
          }
      }
    }
  }

  /** Removes a due date */
  def deleteDueDate(userId: Int) : Future[Unit] = {
    connection { db =>
      db.run(Progress.filter(_.userid === userId).delete).map {
        case 1 => ()
        case n => throw new Exception(s"Removed $n due dates for user: $userId")
      }
    }
  }
}
