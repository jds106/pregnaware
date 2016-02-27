package database.wrappers

import database.ConnectionManager._
import database.schema.Tables._
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/** Session-related functions */
trait SessionWrapper {

  implicit def executionContext: ExecutionContext

  /** Returns a session based on the session id */
  def getSession(sessionId: String): Future[Option[SessionRow]] = {
    connection { db =>
      val query = Session.filter(_.id === sessionId)
      db.run(query.result.headOption)
    }
  }

  /** Returns a session for the user id - will create if missing */
  def setSession(userId: Int): Future[SessionRow] = {
    connection { db =>
      // Return the existing session if one exists
      getExistingSession(userId).flatMap {
        case Some(sessionRow) =>
          Future.successful(sessionRow)

        case None =>
          val sessionId = java.util.UUID.randomUUID().toString
          val insertQuery = Session returning Session
          val action = insertQuery += SessionRow(sessionId, userId)
          db.run(action)
      }
    }
  }

  /** Returns a session (if available) based on user id */
  private def getExistingSession(userId: Int): Future[Option[SessionRow]] = {
    connection { db =>
      val query = Session.filter(_.userid === userId)
      db.run(query.result.headOption)
    }
  }
}
