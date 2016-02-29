package pregnaware.progress

import java.time.LocalDate

import pregnaware.utils.ExecutionWrapper

import scala.concurrent.Future

trait ProgressPersistence extends ExecutionWrapper {
  /** Gets an existing due date */
  def getDueDate(userId: Int) : Future[Option[LocalDate]]

  /** Sets a due date (either adding a due date, or replacing an existing one) */
  def setDueDate(userId: Int, dueDate: LocalDate) : Future[LocalDate]

  /** Removes a due date */
  def deleteDueDate(userId: Int) : Future[Unit]
}
