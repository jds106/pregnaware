package frontend

/** A holder for the user session */
case class SessionEntry(userId: Int, sessionId: String)

/** A holder for new user data */
case class NewUser(displayName: String, email: String, password: String)

/** A holder for returning user data */
case class ReturningUser(email: String, password: String)