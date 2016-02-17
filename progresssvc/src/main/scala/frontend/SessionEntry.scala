package frontend

import java.time.LocalDate

/** A holder for the user session */
case class SessionEntry(userId: Int, sessionId: String)

/** A holder for new user data */
case class NewUser(displayName: String, email: String, dueDate: LocalDate, password: String)

/** A holder for returning user data */
case class ReturningUser(email: String, password: String)