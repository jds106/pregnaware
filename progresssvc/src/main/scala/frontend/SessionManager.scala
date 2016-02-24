package frontend

import java.io.{File, FileWriter}

import org.json4s.jackson.Serialization._
import utils.Json4sSupport._

import scala.io.Source

/**
  * Created by james on 22/02/2016.
  */
trait SessionManager extends SessionPersister {

  // Load in the current user sessions
  private val sessions = new scala.collection.mutable.HashMap[String, SessionEntry]()
  loadSessions.foreach(s => sessions.put(s.sessionId, s))

  /** Returns the session object for the session id */
  def getSession(sessionId: String) : Option[SessionEntry] = {
    sessions.get(sessionId)
  }

  /** Returns a session for this current user - and will create one if no current session exists */
  def getSession(userId: Int): SessionEntry = {
    sessions.values.find(_.userId == userId) match {
      case Some(existingSession) =>
        existingSession

      case None =>
        val session = SessionEntry(userId, java.util.UUID.randomUUID().toString)
        sessions.put(session.sessionId, session)
        saveSession(session)
        session
    }
  }

  /** Removes a session - equivalent to logging out */
  def removeSession(session: SessionEntry) : Unit = {
    sessions.remove(session.sessionId)
    deleteSession(session)
  }
}

trait SessionPersister {
  /** Load all of the sessions into memory */
  def loadSessions: Seq[SessionEntry]

  /** Persist changes */
  def saveSession(session: SessionEntry): Unit

  /** Delete a user session */
  def deleteSession(session: SessionEntry): Unit
}

trait FileSessionPersister extends SessionPersister {

  /** The file root to store the naming suggestions */
  def root: File

  /** Load all of the names into memory */
  def loadSessions: Seq[SessionEntry] = {
    val userIdRegex = "^[0-9]+\\.json$"
    val userFiles = root.listFiles().filter(_.isFile).filter(f => f.getName.matches(userIdRegex))

    userFiles
      .map(f => Source.fromFile(f).mkString)
      .map(s => read[SessionEntry](s))
  }

  /** Persist changes */
  def saveSession(session: SessionEntry): Unit = {
    val file = new File(root, s"${session.userId}.json")
    val writer = new FileWriter(file)
    try {
      writer.write(write(session))
    } finally {
      writer.close()
    }
  }

  /** Delete a user session */
  def deleteSession(session: SessionEntry): Unit = {
    val file = new File(root, s"${session.userId}.json")
    if (file.exists())
      file.delete()
  }
}