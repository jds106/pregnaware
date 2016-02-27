package database

import database.schema.Tables._
import database.ConnectionManager._
import slick.driver.MySQLDriver.api._
import scala.util.{Try, Failure, Success}

import scala.concurrent.Future

/** Wraps up the required database interactions */
class DatabaseWrapper {

  /** ----- Session ----- */
  /** Returns a session based on the session id */
  def getSession(sessionId: String): Future[Option[SessionRow]] = {
    connection { db =>
      val query = Session.filter(_.id === sessionId)
      db.run(query.result.headOption)
    }
  }

  /** Returns a session (if available) based on user id */
  def getExistingSession(userId: Int): Future[Option[SessionRow]] = {
    connection { db =>
      val query = Session.filter(_.userid === userId)
      db.run(query.result.headOption)
    }
  }

  /** Returns a session for the user id - will create if missing */
  def getSession(userId: Int) : Future[SessionRow] = {
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

  /** ----- User ----- */

  /** Get a user by e-mail */
  def getUser(email: String) : Future[Option[UserRow]] = {
    connection { db =>
      val join = (User join Friend)
            .on((u, f) => u.id === f.userid1 || u.id == f.userid2)
            //.filter((u, f) => u.email === email)

      val query = User.filter(_.email === email)
      db.run(query.result.headOption)
    }
  }

  /** Get a user by user id */
  def getUser(userId: Int) : Future[Option[UserRow]] = {
    connection { db =>
      val query = User.filter(_.id === userId)
      db.run(query.result.headOption)
    }
  }


}
