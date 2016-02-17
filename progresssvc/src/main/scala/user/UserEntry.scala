package user

import java.time.LocalDate

case class UserEntry(
  userId: Int,
  displayName: String,
  email: String,
  dueDate: LocalDate,
  passwordHash: String)
