package frontend

import scala.concurrent.Future

/** Session persistence */
trait SessionPersistence {
  def getUserIdFromSession(sessionId: String): Future[Option[Int]]

  /** Returns a session id for the user id - will create if missing */
  def getSession(userId: Int): Future[String]
}
