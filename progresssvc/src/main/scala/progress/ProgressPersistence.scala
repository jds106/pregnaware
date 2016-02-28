package progress

import java.time.LocalDate

import scala.concurrent.Future

/** Progress persistence abstraction */
trait ProgressPersistence {
  /** Gets an existing due date */
  def getDueDate(userId: Int) : Future[Option[LocalDate]]

  /** Sets a due date (either adding a due date, or replacing an existing one) */
  def setDueDate(userId: Int, dueDate: LocalDate) : Future[Boolean]

  /** Removes a due date */
  def deleteDueDate(userId: Int) : Future[Boolean]
}
