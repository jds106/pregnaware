package pregnaware.frontend

import pregnaware.utils.ExecutionWrapper

import scala.concurrent.Future

trait SessionPersistence extends ExecutionWrapper {
  def getUserIdFromSession(sessionId: String): Future[Option[Int]]

  /** Returns a session id for the user id - will create if missing */
  def getSession(userId: Int): Future[String]
}
