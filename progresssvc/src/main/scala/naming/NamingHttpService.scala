package naming

import java.io.{File, FileWriter}

import com.typesafe.scalalogging.StrictLogging
import org.json4s.jackson.Serialization.{read, write}
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import utils.HeaderKeys
import utils.Json4sSupport._

import scala.io.Source

object NamingHttpService {
  val serviceName = "NamingSvc"
}

/** Support name suggestions and rankings */
trait NamingHttpService extends HttpService with NamePersister with StrictLogging {

  type EntryList = scala.collection.mutable.ListBuffer[NamingEntry]
  type EntryHashMap = scala.collection.mutable.HashMap[Int, EntryList]

  /** The routes defined by this service */
  val routes = pathPrefix(NamingHttpService.serviceName) {
    getNames ~ putName ~ deleteName
  }

  // Load in previous naming suggestions
  private val names = new EntryHashMap()
  loadNames.foreach { case (userId, entries) =>
    val entryList = new EntryList
    entries.foreach(e => entryList += e)
    names.put(userId, entryList)
  }

  private def getEntries(userId: Int) = {
    names.get(userId) match {
      case Some(entryList) =>
        entryList

      case None =>
        val entryList = new EntryList
        names.put(userId, entryList)
        entryList
    }
  }

  /** Returns the current list of known names */
  def getNames: Route = get {
    path("names") {
      userIdFromHeader { userId =>
        complete(StatusCodes.OK -> NamingEntries(names.getOrElse(userId, Seq.empty[NamingEntry])))
      }
    }
  }

  /** Adds a new name to the list */
  def putName: Route = put {
    path("name") {
      userIdFromHeader { userId =>
        entity(as[NamingEntry]) { entry =>
          val currentEntries = getEntries(userId)
          val nameId = if (currentEntries.isEmpty) 1 else currentEntries.map(_.nameId).max + 1
          val persistedEntry = entry.copy(nameId = nameId)
          currentEntries += persistedEntry
          saveNames(userId, currentEntries)
          complete(StatusCodes.OK -> persistedEntry)
        }
      }
    }
  }

  def deleteName: Route = delete {
    path("name" / IntNumber) { nameId =>
      userIdFromHeader { userId =>
        names.get(userId) match {
          case None =>
            logger.error(s"Could not find any entries for user $userId")
            complete(StatusCodes.BadRequest)

          case Some(currentEntries) =>
            currentEntries.find(_.nameId == nameId).foreach(e => currentEntries -= e)
            saveNames(userId, currentEntries)
            complete(StatusCodes.OK)
        }
      }
    }
  }

  private def userIdFromHeader(handler: Int => Route): Route = {
    headerValueByName(HeaderKeys.EntryId)(s => handler(s.toInt))
  }
}

trait NamePersister {
  /** Load all of the names into memory */
  def loadNames: Map[Int, Seq[NamingEntry]]

  /** Persist changes */
  def saveNames(userId: Int, entryList: Seq[NamingEntry]): Unit
}

trait FileNamePersister extends NamePersister {

  /** The file root to store the naming suggestions */
  def root: File

  /** Load all of the names into memory */
  def loadNames: Map[Int, Seq[NamingEntry]] = {
    val userIdRegex = "^[0-9]+\\.json$"
    val userFiles = root.listFiles().filter(_.isFile).filter(f => f.getName.matches(userIdRegex))

    userFiles
      .map { f => (f.getName.replace(".json", "").toInt, Source.fromFile(f).mkString) }
      .map { case (userId, s) => (userId, read[NamingEntries](s)) }
      .map { case (userId, nm) => userId -> nm.entries }
      .toMap
  }

  /** Persist changes */
  def saveNames(userId: Int, entryList: Seq[NamingEntry]): Unit = {
    val entries = NamingEntries(entryList)

    val file = new File(root, s"$userId.json")
    val writer = new FileWriter(file)
    try {
      writer.write(write(entries))
    } finally {
      writer.close()
    }
  }
}