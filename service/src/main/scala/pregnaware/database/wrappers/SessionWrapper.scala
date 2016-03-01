package pregnaware.database.wrappers

import java.time.Instant

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.frontend.SessionPersistence
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

trait SessionWrapper extends SessionPersistence {

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
          val action = Session += SessionRow(sessionId, userId, Instant.now().toEpochMilli)
          db.run(action).map(_ => sessionId)
      }
    }
  }
}
