package pregnaware.database.wrappers

import java.sql.Date
import java.time.LocalDate

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** Due-date related functions */
trait DueDateWrapper extends CommonWrapper {
  /** Sets a due date (either adding a due date, or replacing an existing one) */
  def setDueDate(userId: Int, dueDate: LocalDate) : Future[LocalDate] = {
    connection { db =>
      val query = User.filter(_.id === userId).map(_.duedate)
      val action = query.update(Some(Date.valueOf(dueDate)))
      db.run(action).map {
        case 1 => dueDate
        case n => throw new Exception(s"Set due date on $n users with $userId")
      }
    }
  }

  /** Removes a due date */
  def deleteDueDate(userId: Int) : Future[Unit] = {
    connection { db =>
      val query = User.filter(_.id === userId).map(_.duedate)
      val action = query.update(None)
      db.run(action).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted due date on $n users with $userId")
      }
    }
  }
}
