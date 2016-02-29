package database.wrappers

import database.ConnectionManager._
import database.schema.Tables._
import frontend.SessionPersistence
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/** Session-related functions */
trait SessionWrapper extends SessionPersistence {

  implicit def executionContext: ExecutionContext

  /** Returns a session based on the session id */
  def getUserIdFromSession(sessionId: String): Future[Option[Int]] = {
    connection { db =>
      val query = Session.filter(_.id === sessionId).map(_.userid)
      db.run(query.result.headOption)
    }
  }

  /** Returns a session for the user id - will create if missing */
  def getSession(userId: Int): Future[String] = {
    connection { db =>
      // Return the existing session if one exists
      db.run(Session.filter(_.userid === userId).map(_.id).result.headOption).flatMap {
        case Some(sessionId) =>
          Future.successful(sessionId)

        case None =>
          val sessionId = java.util.UUID.randomUUID().toString
          val insertQuery = Session returning Session
          val action = insertQuery += SessionRow(sessionId, userId)
          db.run(action).map(_ => sessionId)
      }
    }
  }
}
