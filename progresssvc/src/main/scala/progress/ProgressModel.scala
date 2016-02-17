package progress

import java.time.LocalDate

/** Simple due-date model */
case class ProgressModel(
  dueDate: LocalDate,
  daysPassed: Long,
  daysRemaining: Long)
