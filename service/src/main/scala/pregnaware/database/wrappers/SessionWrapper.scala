package pregnaware.database.wrappers

import java.time.Instant

import com.typesafe.scalalogging.StrictLogging
import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.frontend.SessionPersistence
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

trait SessionWrapper extends SessionPersistence with StrictLogging {

  /** Returns a session based on the session id */
  def getUserIdFromSession(sessionId: String): Future[Option[Int]] = {
    logger.info("Loading user id for session...")

    val userIdFetchFut = connection("GetUserIdFromSession") { db =>
      val query = Session.filter(_.id === sessionId).map(_.userid)

      // Return the existing user if one exists (and update the last access time)
      db.run(query.result.headOption)
    }

    userIdFetchFut.map {
      case Some(userId) =>
        // Schedule an update to the access time, but don't wait for it to finish before returning the session id
        connection("UpdateSessionAccessTime") { db =>
          db.run(Session.filter(_.userid === userId).map(_.accesstime).update(Instant.now.toEpochMilli))
        }
        Some(userId)

      case None =>
        None
    }
  }

  /** Returns a session for the user id - will create if missing */
  def getSession(userId: Int): Future[String] = {
    val sessionIdFut = connection("GetSession") { db =>
      db.run(Session.filter(_.userid === userId).map(_.id).result.headOption)
    }

    sessionIdFut.flatMap {
      case Some(sessionId) =>
        // Schedule an update to the access time, but don't wait for it to finish before returning the session id
        connection("UpdateSessionAccessTime") { db =>
          db.run(Session.filter(_.userid === userId).map(_.accesstime).update(Instant.now.toEpochMilli))
        }
        Future.successful(sessionId)

      case None =>
        val sessionId = java.util.UUID.randomUUID().toString
        val action = Session += SessionRow(sessionId, userId, Instant.now().toEpochMilli)
        connection("AddSession") { db =>
          db.run(action).map(_ => sessionId)
        }
    }
  }
}
